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
	public static List<MovieOnReturn> movies;
	public static HashMap<Integer, MovieOnReturn> mapMov;
    public static void main(String[] args) {
    	port(Integer.valueOf(System.getenv("PORT")));
    	
    	get("/", (request, response) -> 
    	{
    		return "Message: ";
		});
    	
        get("/search/movie/:movieName", (request, response) -> 
        {

        	String query = request.params(":movieName");
        	String movie = "http://api.themoviedb.org/3/search/movie?query=";
        	String API = "&api_key=c2dcd458445148b91ed151b2a41a3c22&page=";
        	query = URLEncoder.encode(query, "UTF-8");
        	String URI = movie+query+API;
        	//URI = URLEncoder.encode(URI, "UTF-8");

        	//refactor it, too tired, you'll work it out
        	//put the running code into the queryNameFinder Class
        	
        	
        	JSONObject jsonResults = getJSON(URI);
        	int totPages = (int)jsonResults.get("total_pages");
        	JSONArray returnJson = new JSONArray();
        	//if statement? if too many pages, tell user to refine search	
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
        
        //if score below 0 then dump that shits
        
        get("/request/movie/:movieId", (request, response) ->
        {    	
        	movies = Collections.synchronizedList(new ArrayList<MovieOnReturn>());
        	mapMov = new HashMap();
        	
        	final long startTime = System.currentTimeMillis();
        	String baseURI = "http://api.themoviedb.org/3/movie/";
            String endURI = "?api_key=c2dcd458445148b91ed151b2a41a3c22&append_to_response=credits,keywords";
            String query = request.params(":movieId");
            query = URLEncoder.encode(query, "UTF-8");
            String URI = baseURI + query + endURI;
            
            JSONObject s = getJSON(URI);
            final long endTime = System.currentTimeMillis();
            System.out.println("Total execution time: " + (endTime - startTime) );
            MovieOnGet movie = new MovieOnGet(s, "genres");
            return getMovies(movie);	
    	});
    }

	private static JSONArray getMovies(MovieOnGet movie) throws InterruptedException {
		QueryBuilder qb = new KeywordsQuery(movie, 1);
		qb.createQueries(); 	
		QueryExecutor qe = new QueryExecutor(qb.getQueries(), movies, movie, false);
		qe.run();
		
		QueryBuilder staff = new StaffQuery(movie, 1);
		staff.createQueries();
		QueryExecutor qe2 = new QueryExecutor(staff.getQueries(), movies, movie, true);
		qe2.run();
		
		int totKeyPages = (int)qe.getJson().get("total_pages");
		int totStaffPages = (int)qe2.getJson().get("total_pages");

		if(totKeyPages>1||totStaffPages>1)
		{
			ExecutorService executor = Executors.newFixedThreadPool(totKeyPages+totStaffPages);
			int sum = (totKeyPages+(totStaffPages-1));
			for(int j = 2; j<=sum; j++)
		    {
				if(j<=totKeyPages)
				{
			    	QueryBuilder qbj = new KeywordsQuery(movie, j);
			    	qbj.createQueries(); 	
			        QueryExecutor qej = new QueryExecutor(qbj.getQueries(), movies, movie, false);
			        
			        executor.submit(qej);
				}
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
		for(int i = 0; i<movies.size(); i++)
		{
			listSort();
		}
		HashMap<Integer, JSONObject> jsonMap = new HashMap<>();
		JSONArray sj = parseJSONList(movies, jsonMap); 

		return sj;
	}

    public static void lister(JSONObject json, MovieOnGet m, Boolean isStaff)
    {
    	//add some dynamism
    	List<MovieOnReturn> tempMovies = new ArrayList<MovieOnReturn>();
    	JSONArray j = (JSONArray) json.get("results");
    	for(int i = 0; i<j.length(); i++)
    	{    	
    		JSONObject js = j.getJSONObject(i);
    		MovieOnReturn newMov = new MovieOnReturn(js, "genre_ids", isStaff);
    		int id = newMov.getId();
    		id = id+2;
    		if(newMov.getId()!=m.getId())
    		{
    			scoreMovie(m, newMov);
    			//PROBABLY A TERRIBLE IDEA, REMOVE IF STATEMENT IF NO RESULTS START GETTING RETURNED
    			if(newMov.getScore()>0)
    			{
    				if(mapMov.containsKey(newMov.getId()))
    				{
    					newMov.appendScore(mapMov.get(newMov.getId()).getScore());
    				}
    				else
    				{
    					mapMov.put(newMov.getId(), newMov);
    				}
    				//add to map, check the maps
    				tempMovies.add(newMov);
    				
    			}
    		}
    		else{
    			//tempMovies.add(i, new MovieOnReturn());
    		}
    	}
    	movies.addAll(tempMovies);
	}
    public static void listSort()
    {
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
    	 * 
    	 * INSTEAD OF SENDING OFF ONE THREADED QUERY FOR DIRECTOR AND WRITER
    	 * SEND OFF 2 REQUESTS, 1 FOR DIRECTOR, 1 FOR WRITER
    	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
    	 * MARK EACH AS SUCH, WHEN ADDING TO LIST, CHECK IF ALREADY THERE, IF IT IS, ADD POINTS TO IT RATHER THAN DOUBLE ENTRIES
    	 * TRY DOING THIS EXACT THING B4 DOING ABOVE, MIGHT WORK FINE JUST BY DOING THAT? FIIK
    	 * 
    	 */
    	int scrValue = 100/mOne.getGenres().length;
    	int oneLength = mOne.getGenres().length;
		int twoLength = mTwo.getGenres().length;
		for(int iOne = 0; iOne<oneLength; iOne++)
    	{
    		Boolean found = false;
    		//second movie loop
    		for(int iTwo = 0; iTwo<twoLength; iTwo++)
    		{
    			if(mOne.getGenres(iOne)==mTwo.getGenres(iTwo))
    			{
    				found = true;
    				break;
    			}
    		}
    		if(found)
    		{
    			if(mTwo.isStaff())
    			{
    				mTwo.appendScore((int) (scrValue+(scrValue+10)));
    			}
    			else
    			{
    				mTwo.appendScore(scrValue);
    			}
    		}
    	}
    	if(oneLength!=twoLength)
    	{
    		mTwo.appendScore((Math.abs((oneLength-twoLength))*(scrValue/2))*-1);
    	}
    	//System.out.println(mTwo.getScore());
    }
    public static JSONArray parseJSONList (List<MovieOnReturn> movies, HashMap<Integer, JSONObject> jsonMap)
    {
    	//results gathers the json objects together in an array
    	HashMap<Integer, JSONObject> garbageMovies = new HashMap<>();
    	//maxListSize is a threshold for the results, it will stay at 10 unless the returned results 
    	//at 10 are still really good matches
    	int maxListSize = 10;
    	//threshold will be shrunk to a minimum if the list dosnt contain enough movies 
    	//to satisfy the conditions
    	if(movies.size()<10)
    	{
    		maxListSize = movies.size()-1;
    	}
    	//simple variables to control the flow of the while loop
    	int counter = 0;
    	Boolean goodMatch = true;
    	//max values controls access to the returning json array
    	//the barrier for entry is initially set very high(the highest Score in the list), but will shrink 
    	//if there are not enough values in the JSONArray
    	int maxValues = movies.get(counter).getScore();
    	int io = -1;
    	while(counter<maxListSize&&goodMatch==true)
    	{
    		io++;
    		int isp = movies.get(counter).getScore();
    		int asd = movies.get(counter).getId();
    		System.out.println("Id: "+asd+" Score: "+isp);
    		if(movies.get(counter).getScore()>=maxValues)
    		{
    			//if value is not in map already
    			if(jsonMap.get(movies.get(counter).getId()) == null)
    			{
		    		jsonMap.put(movies.get(counter).getId(), movies.get(counter).getJson());
    			}
    			counter++;
    		}
    		else if(jsonMap.size()<maxListSize)
    		{
    			maxValues-=10;
    		}
    		else
    		{
    			goodMatch=false;
    		}
    		//if after respectable amount of movies, scores are still very strong, then carry on
    		
    		try{
	    		if(counter==(maxListSize)&&movies.get(counter).getScore()>=90)
				{
	    			maxListSize++;
				}
    		}
    		catch(Exception ex)
    		{
    			System.out.println("");
    		}
    		
    	}
    	JSONArray results = new JSONArray();
    	for(Entry<Integer, JSONObject> value: jsonMap.entrySet()) {
    		results.put(value.getValue());
    	}
    	return results;
    
    }    
    public static JSONObject getJSON(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
          BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
          String jsonText = readAll(rd);
          JSONObject json = new JSONObject(jsonText);
          return json;
        } finally {
          is.close();
        }
    }
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }
        return sb.toString();
      }
    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4596; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}