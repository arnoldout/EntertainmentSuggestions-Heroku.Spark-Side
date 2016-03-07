package main.java;

import static spark.Spark.*;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.*;

public class Main {
	static List<MovieOnReturn> movies = Collections.synchronizedList(new ArrayList<MovieOnReturn>());
    public static void main(String[] args) {
    	//port(Integer.valueOf(System.getenv("PORT")));
    	//Movie m = new Movie(new int[]{18,28,80,53},7, 211, 132);
    	get("/", (request, response) -> 
    	{
    		return "Message: ";
		});
    	
        get("/search/movie/:movieName", (request, response) -> 
        {
        	String baseURI = "http://api.themoviedb.org/3/search/movie?query=";
            String apiKey = "&api_key=c2dcd458445148b91ed151b2a41a3c22";
            String query =request.params(":movieName");
            query = URLEncoder.encode(query, "UTF-8");
            String URI = baseURI + query + apiKey;
            JSONObject s = readJsonFromUrl(URI);
            return "movie: " + s.toString();
        });
        
        get("/request/movie/:movieId", (request, response) ->
        {
        	final long startTime = System.currentTimeMillis();
        	String baseURI = "http://api.themoviedb.org/3/movie/";
            String endURI = "?api_key=c2dcd458445148b91ed151b2a41a3c22&append_to_response=credits,keywords";
            String query = request.params(":movieId");
            query = URLEncoder.encode(query, "UTF-8");
            String URI = baseURI + query + endURI;
            
            JSONObject s = readJsonFromUrl(URI);
            final long endTime = System.currentTimeMillis();
            System.out.println("Total execution time: " + (endTime - startTime) );
            MovieOnGet movie = new MovieOnGet(s, "genres");
            QueryBuilder qb = new QueryBuilder(movie, 1);
            qb.createQueries(); 	
        	QueryExecutor qe = new QueryExecutor(qb.getQueries());
            qe.runQuery();
            
            movies = lister(qe.getJson(), movie, movies);
            int i = (int)qe.getJson().get("total_pages");
            if(i>1)
            {
	            for(int j = 2; j<i; j++)
	            {
	            	QueryBuilder qbj = new QueryBuilder(movie, j);
	            	Thread t = new Thread() {
	            	    public void run() {
	                        qb.createQueries(); 	
	                    	QueryExecutor qej = new QueryExecutor(qb.getQueries());
	                        qe.runQuery();
	                        //mutex lock here
	                        movies = lister(qe.getJson(), movie, movies);
	            	    }
	            	};	
	            	t.start();   	
	            }
            }
            JSONArray sj = parseJSONList(movies); 
        	return sj;
    	});
    }
    
    public static List<MovieOnReturn> lister(JSONObject json, MovieOnGet m, List<MovieOnReturn> movies)
    {
    	JSONArray j = (JSONArray) json.get("results");
    	for(int i = 0; i<j.length(); i++)
    	{    	
    		JSONObject js = j.getJSONObject(i);
    		MovieOnReturn newMov = new MovieOnReturn(js, "genre_ids");
    		if(newMov.getId()!=m.getId())
    		{
    			movies.add(newMov);
    			scoreMovie(m,movies.get(i));
    		}
    		else{
    			movies.add(i, new MovieOnReturn());
    		}
    		
    	}
    	Collections.sort(movies, new Comparator<MovieOnReturn>() {
            @Override
            public int compare(MovieOnReturn o1, MovieOnReturn o2) {
                return Integer.compare(o2.getScore(), o1.getScore());
            }
        });
    	return movies;
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
    	while(counter<maxListSize&&goodMatch==true)
    	{
    		if(movies.get(counter).getScore()>=100)
    		{
	    		results.put(movies.get(counter).getJson());
	    		counter++;
    		}
    		else
    		{
    			goodMatch=false;
    		}
    		if(counter==(maxListSize)&&movies.get(counter).getScore()>=100)
			{
    			maxListSize++;
			}
    	}
    	System.out.println("RETURNED");
    	return results;
    
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
     
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
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