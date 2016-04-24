package main.java;

import static spark.Spark.*;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.*;

public class Main {
	//movies will store the MovieOnReturn objects
	public static List<MovieOnReturn> movies;
	public static HashMap<Integer, MovieOnReturn> mapMov;
	
    public static void main(String[] args) {
    	//set active port requests connect through
    	port(Integer.valueOf(System.getenv("PORT")));
    	
    	//basic help response to a blank call to the webpage
    	get("/", (request, response) -> 
    	{
    		return "Invalid Request\nTo search for a movie use: /search/movie/:movieName\nTo request a movie use:/request/movie/:movieId";
		});
    	
    	//search db for a list of movies based on the parameter movieName
        get("/search/movie/:movieName", (request, response) -> 
        {
        	/*
        	 * This get request/response makes a similar call to the movieDB
        	 * and on return, it will refine the search to only return movies that are above 
        	 * a certain threshold.
        	 * Due to the open-ness of theMovieDB, there can sometimes be junk movies that need to be weeded out
        	 * 
        	 * To send a Movie Search request to the moviedb, a url like this needs to be built: 
        	 * api.themoviedb.org/3/search/movie?query=The%20GodFather&api_key=c2dcd458445148b91ed151b2a41a3c22
        	 * The URL will mostly stay constant, the only part that needs a variable is the query="VARIABLE"
        	 * This variable is taken from :movieName in the current URL 
        	 * 
        	 */
        	String query = request.params(":movieName");
        	String movie = "http://api.themoviedb.org/3/search/movie?query=";
        	String API = "&api_key=c2dcd458445148b91ed151b2a41a3c22&page=";
        	query = URLEncoder.encode(query, "UTF-8");
        	//URI now stores the functioning get request
        	String URI = movie+query+API;
        	//the get request responds with all the movies that match :movieName
        	//the matches are returned in JSON format
        	
        	//getJSON makes the get request and returns the JSON into jsonResults
        	JSONObject jsonResults = getJSON(URI);
        	//totPages refers to the total amount of JSON pages there are
        	int totPages = (int)jsonResults.get("total_pages");
        	//returnJson will store the JSON objects for any valid movies
        	JSONArray returnJson = new JSONArray();
        	
        	/*
        	 * The returned JSON will include a totalPages variable, this indicates how many pages of JSON was matched to
        	 * the query.
        	 * Each page is given a worker thread, and that thread must parse a page of JSON
        	 * while parsing, it will extract any movies that are legible.
        	 * -- TheMovieDB is open for anyone to edit, so there can be many dud results returned. 
        	 * The algorithm removes these duds. Adding real matches to the JSONArray
        	 * 
        	 * Once all threads are complete, the pool is joined to the main process, and the JSONArray, now populated
        	 * is returned to the user
        	 */
        	ExecutorService threadPool = Executors.newFixedThreadPool(totPages);
    		for(int pageLoop = 1 ; pageLoop<=totPages; pageLoop++)
    		{
    			JSONArray jsArr = (JSONArray) jsonResults.get("results");
    			threadPool.submit(new QueryNameFinder(jsArr, returnJson));
    			if(pageLoop+1<=totPages)
    			{
    				jsonResults = getJSON(URI+(pageLoop+1));
    			}
    		}
    		threadPool.shutdown();
    		threadPool.awaitTermination(5000, TimeUnit.MINUTES);
    		
    		
    		return returnJson.toString();

        });
        
        get("/request/movie/:movieId", (request, response) ->
        {   
        	/*
        	 * Suggestions are sought for the user when they do an ID request
        	 * An initial call to theMovieDB server requests information about the client's desired movie.
        	 */
        	movies = Collections.synchronizedList(new ArrayList<MovieOnReturn>());
        	mapMov = new HashMap<Integer, MovieOnReturn>();
        	
        	String baseURI = "http://api.themoviedb.org/3/movie/";
            String endURI = "?api_key=c2dcd458445148b91ed151b2a41a3c22&append_to_response=credits,keywords";
            String query = request.params(":movieId");
            query = URLEncoder.encode(query, "UTF-8");
            String URI = baseURI + query + endURI;
            
            JSONObject json = getJSON(URI);
            
            //the MovieOnGet class parses JSON returned from the server.  
            MovieOnGet movie = new MovieOnGet(json, "genres");
            
            try
            {
            	JSONArray results = getMovies(movie);
            	return results;
            }
            catch(InterruptedException e)
            {
            	response.status(500);
            	return "Server Error";
            }
    	});
    }

	private static JSONArray getMovies(MovieOnGet movie) throws InterruptedException {
		/*
		 *	The program creates a multitude of API get requests. 
		 *	Firstly it will create a large discover movie request to theMovieDB using all of the keywords that are 
		 *	associated with the requested movies as parameters. 
		 *	Secondly it makes a similar request buy using staff such as writers and directors as parameters
		 *
		 */
		
		QueryBuilder keyBuilder = new KeywordsQuery(movie, 1);
		keyBuilder.createQueries(); 	
		QueryExecutor keyExecutor = new QueryExecutor(keyBuilder.getQueries(), movies, movie, false);
		keyExecutor.run();
		
		QueryBuilder staffBuilder = new StaffQuery(movie, 1);
		staffBuilder.createQueries();
		QueryExecutor staffExecutor = new QueryExecutor(staffBuilder.getQueries(), movies, movie, true);
		staffExecutor.run();
		
		/*
		 * Both requests will have a multitude of JSON pages.
		 * The total amount of pages is extracted from the JSON and stored as seperate integers
		 */
		int totKeyPages = (int)keyExecutor.getJson().get("total_pages");
		int totStaffPages = (int)staffExecutor.getJson().get("total_pages");

		//only on rare occasions will this be false
		if(totKeyPages>1||totStaffPages>1)
		{
			//a new thread is needed for each page of key and staff JSON
			ExecutorService executor = Executors.newFixedThreadPool(totKeyPages+totStaffPages);
			//sum controls the loop 
			int sum = (totKeyPages+(totStaffPages-1));
			for(int j = 2; j<=sum; j++)
		    {
				//The first half of the loop makes the key queries
				if(j<=totKeyPages)
				{
			    	QueryBuilder qbj = new KeywordsQuery(movie, j);
			    	qbj.createQueries(); 	
			        QueryExecutor qej = new QueryExecutor(qbj.getQueries(), movies, movie, false);
			        
			        executor.submit(qej);
				}
				//The second half makes the staff queries
				else
				{
					int pageNo = j-totKeyPages;
					QueryBuilder qbj = new StaffQuery(movie, pageNo);
			    	qbj.createQueries(); 	
			        QueryExecutor qej = new QueryExecutor(qbj.getQueries(), movies, movie, true);
			        executor.submit(qej);
				}
		    }
			executor.shutdown();
			executor.awaitTermination(50000, TimeUnit.MINUTES);
		}
		/*
		 * At this point the List "movies" is full of potential movies, with no doubles
		 * The list here is sorted by order of score
		 */
		for(int i = 0; i<movies.size(); i++)
		{
			listSort();
		}
		/* A map then will store the movies, parseJSONList will refine the list further
		 * and dynamically add at least 10 movies.
		 */
		HashMap<Integer, JSONObject> jsonMap = new HashMap<>();
		JSONArray finalArr = parseJSONList(movies, jsonMap); 

		return finalArr;
	}

    public static void extractMovies(JSONObject json, MovieOnGet m, Boolean isStaff)
    {
    	/*
    	 * ExtractMovies takes in a page of json, and breaks it down into movieOnReturn objects
    	 * Each movie obect is scored and added to the mapMov hashMap
    	 */
    	List<MovieOnReturn> tempMovies = new ArrayList<MovieOnReturn>();
    	//remove additional parameters from json
    	JSONArray pageArr = (JSONArray) json.get("results");
    	//loop through the page of JSON
    	for(int i = 0; i<pageArr.length(); i++)
    	{    	
    		JSONObject jsonMovie = pageArr.getJSONObject(i);
    		MovieOnReturn newMov = new MovieOnReturn(jsonMovie, "genre_ids", isStaff);
    		
    		if(newMov.getId()!=m.getId())
    		{
    			//score the movie
    			scoreMovie(m, newMov);
    			if(newMov.getScore()>0)
    			{
    				//check if movie exists already
    				if(mapMov.containsKey(newMov.getId()))
    				{
    					newMov.appendScore(mapMov.get(newMov.getId()).getScore());
    				}
    				else
    				{
    					//if movie is not in map, add to map
    					mapMov.put(newMov.getId(), newMov);
    				}
    				tempMovies.add(newMov);
    			}
    		}
    	}
    	movies.addAll(tempMovies);
	}
    public static void listSort()
    {
    	//sort movies list in order of score
    	Collections.sort(movies, new Comparator<MovieOnReturn>() {
            @Override
            public int compare(MovieOnReturn o1, MovieOnReturn o2) {
                return Integer.compare(o2.getScore(), o1.getScore());
            }
        });
    }

    public static void scoreMovie(MovieOnGet mOne, MovieOnReturn mTwo)
    {
    	/*
    	 * To score a movie, I get to movies, the one to be scored, and the originally client requested movie.
    	 * I make comparisons between the two, and depending on how similar they are, I give them a score. 
    	 * 
    	 * I initially get an integer value of how many genres are associated with either movie, stored as one and twoLength
    	 * As well as that, I determine how valuable a single genre match is worth. I do this by dividing 100 by the amount of 
    	 * 	genres in the original movie. 
    	 * Whenever a match in genres between the two is found, mTwo gets a score appended to its current value
    	 */
    	int scrValue = 100/mOne.getGenres().length;
    	int oneLength = mOne.getGenres().length;
		int twoLength = mTwo.getGenres().length;
		int foundCounter = 0;
		/*
		 * Here the program loops through the original movie's genres 
		 * 	and the new movie's genres, if a match was found, the inner loop is broken and thrown into the scoring algorithm
		 */
		for(int iOne = 0; iOne<oneLength; iOne++)
    	{
    		Boolean found = false;
    		//second movie loop
    		for(int iTwo = 0; iTwo<twoLength; iTwo++)
    		{
    			if(mTwo.getId()==1443)
    			{
    				found = false;
    			}
    			if(mOne.getGenres(iOne)==mTwo.getGenres(iTwo))
    			{
    				//genre match found, exit loop
    				found = true;
    				foundCounter++;
    				break;
    			}
    		}
    		//when a match has been found, enter the scoring algorithm
    		if(found)
    		{
    			if(mTwo.isStaff())
    			{
    				//A genre match on a movie that also has similar staff is extra valuable
    				//in this instance, the movie gets an extra 10 points
    				mTwo.appendScore((int) (scrValue+(scrValue*4)));
    			}
    			else
    			{
    				mTwo.appendScore(scrValue);
    			}
    			found = false;
    		}
    	}
		//if a movie didn't match all the genres, a penalty is imposed
    	if(oneLength!=foundCounter)
    	{
    		if(mTwo.isStaff())
			{
				//A genre match on a movie that also has similar staff is extra valuable
				//in this instance, the movie gets an extra 10 points
    			mTwo.appendScore((int) ((Math.abs((oneLength-foundCounter))*(scrValue*1.5))*-4));
			}
			else
			{
				mTwo.appendScore((int) ((Math.abs((oneLength-foundCounter))*(scrValue/3))*-1));
			}
    	}
    	//if a movie had more genres than were matched, a penalty is imposed
    	if(foundCounter!=twoLength)
    	{
    		if(mTwo.isStaff())
			{
				//A genre match on a movie that also has similar staff is extra valuable
				//in this instance, the movie gets an extra 10 points
    			mTwo.appendScore((int) ((Math.abs((twoLength-foundCounter))*(scrValue*1.5))*-4));
			}
			else
			{
				mTwo.appendScore((int) ((Math.abs((twoLength-foundCounter))*(scrValue/3))*-1));
			}
    	}
    }
    public static JSONArray parseJSONList (List<MovieOnReturn> movies, HashMap<Integer, JSONObject> jsonMap)
    {
    	/*
    	 * Here the final list of scored movie objects are refined down to the best possible results
    	 * At this point the movies are sorted by score, so the best results just need to be taken from the top of the list
    	 * 
    	 * Initially the returning list is given a max size of 10, or if less than 10 movies are available, the max is set to the size of the list-1
    	 */
    	int maxListSize = 1;
    	//simple variables to control the flow of the while loop
    	int counter = 0;
    	Boolean goodMatch = true;
    	/*
    	 * Max values controls access to the returning json array
    	 * The barrier for entry is initially set very high(the highest Score in the list), but will shrink if there are not enough values in the JSONArray
    	 * 
    	 */
    	int maxValues = movies.get(counter).getScore();
    	 
    	//Loops until either there's enough results or the results arn't of a good enough score
    	while(counter<=maxListSize&&goodMatch==true)
    	{
    		try{
	    		//check if this movie is as good as the current score threshold
	    		if(movies.get(counter).getScore()>=maxValues)
	    		{
	    			//if value is not in map already, put in map	
	    			if(!jsonMap.containsKey(movies.get(counter).getId()))
	    			{
	    				JSONObject tempObj= movies.get(counter).getJson();
	    				tempObj.put("Score", movies.get(counter).getScore());
			    		jsonMap.put(movies.get(counter).getId(), tempObj);
	    			}
	    			counter++;
	    		}
	    		//if the score wasn't good enough, and the list isn't full yet, move the threshold to accommodate the next movie
	    		else if(jsonMap.size()<maxListSize&&movies.get(counter+1).getScore()>75)
	    		{
	    			maxValues=movies.get(counter+1).getScore();
	    		}
	    		//otherwise the list is full, and there are no longer any good matches, the loop exits
	    		else
	    		{
	    			goodMatch=false;
	    		}
    		}
    		catch(IndexOutOfBoundsException e)
    		{
    			goodMatch = false;
    		}
    		
    		/*
    		 * The length of the list is extended if good movies are still available to be chosen
    		 */
			int currentScr = movies.get(counter).getScore();
    		if(counter==(maxListSize))
			{
    			if(currentScr>75&&maxListSize<=15)
    			{
	    			maxListSize++;
	    			goodMatch = true;
    			}
			}
    	}
    	/*
    	 * Once exited the loop, a for each statement pulls each key value pair from the map
    	 * 	and adds the value to results. When finished, results will then contain all the JSON for the recommended movies
    	 */
    	
    	JSONArray results = new JSONArray();
    	for(Entry<Integer, JSONObject> value: jsonMap.entrySet()) {
    		results.put(value.getValue());
    	}
    	return results;
    }    

    public static JSONObject getJSON(String url) throws IOException, JSONException {
    	//Stream takes in data from the url
        InputStream stream = new URL(url).openStream();
        try {
        	//try to read from the stream
	          BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
	          //get json from the reader
	          JSONObject json = new JSONObject(readAll(reader));
	          return json;
        } 
        finally {
        	//close the stream
          stream.close();
        }
    }
    //method to read all of the JSON using a string builder
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }
        return sb.toString();
      }
    //Heroku method for getting the port Heroku is running on
    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4596; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}