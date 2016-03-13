package main.java;

import static spark.Spark.*;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.*;

public class Main {
	public static List<MovieOnReturn> movies;
    public static void main(String[] args) {
    	//port(Integer.valueOf(System.getenv("PORT")));
    	//Movie m = new Movie(new int[]{18,28,80,53},7, 211, 132);
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

	private static Object getMovies(MovieOnGet movie) throws InterruptedException {
		QueryBuilder qb = new QueryBuilder(movie, 1);
		qb.createQueries(); 	
		QueryExecutor qe = new QueryExecutor(qb.getQueries(), movies, movie);
		qe.run();
		
		int totPages = (int)qe.getJson().get("total_pages");
		if(totPages>1)
		{
			ExecutorService executor = Executors.newFixedThreadPool(totPages);
			for(int j = 2; j<=totPages; j++)
		    {
		    	QueryBuilder qbj = new QueryBuilder(movie, j);
		    	qbj.createQueries(); 	
		        QueryExecutor qej = new QueryExecutor(qbj.getQueries(), movies, movie);
		             
		        executor.submit(qej);
		    }
			executor.shutdown();
			executor.awaitTermination(50000, TimeUnit.MINUTES);
		}
		for(int i = 0; i<movies.size(); i++)
		{
			listSort();
		}
		
		JSONArray sj = parseJSONList(movies); 

		return sj;
	}

    public static void lister(JSONObject json, MovieOnGet m)
    {
    	//add some dynamism
    	List<MovieOnReturn> tempMovies = new ArrayList<MovieOnReturn>();
    	JSONArray j = (JSONArray) json.get("results");
    	for(int i = 0; i<j.length(); i++)
    	{    	
    		JSONObject js = j.getJSONObject(i);
    		MovieOnReturn newMov = new MovieOnReturn(js, "genre_ids");
    		if(newMov.getId()!=m.getId())
    		{
    			scoreMovie(m, newMov);
    			//PROBABLY A TERRIBLE IDEA, REMOVE IF STATEMENT IF NO RESULTS START GETTING RETURNED
    			if(newMov.getScore()>0)
    			{
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
    	int scrValue = 100/mOne.getGenres().length;
    	//first movie loop
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
    			mTwo.appendScore(scrValue);
    		}
    	}
    	if(oneLength!=twoLength)
    	{
    		mTwo.appendScore((Math.abs((oneLength-twoLength))*(scrValue/4))*-1);
    	}
    	System.out.println(mTwo.getScore());
    }
    public static JSONArray parseJSONList (List<MovieOnReturn> movies)
    {
    	//	movies.add(new MovieOnReturn());
    	JSONArray results = new JSONArray();
    	//results.put("results");
    	int maxListSize = 10;
    	if(movies.size()<10)
    	{
    		maxListSize = movies.size();
    	}
    	int counter = 0;
    	Boolean goodMatch = true;
    	int maxValues = 100;
    	while(counter<maxListSize&&goodMatch==true)
    	{
    		if(movies.get(counter).getScore()>=maxValues)
    		{
	    		results.put(movies.get(counter).getJson());
	    		counter++;
    		}
    		else if(results.length()<maxListSize)
    		{
    			maxValues-=10;
    		}
    		else
    		{
    			goodMatch=false;
    		}
    		//if after respectable amount of movies, scores are still very strong, then carry on
    		if(counter==(maxListSize)&&movies.get(counter).getScore()>=90)
			{
    			maxListSize++;
			}
    	}
    	System.out.println("RETURNED");
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