package main.java;

import static spark.Spark.*;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Calendar;
import org.json.*;

public class Main {
    public static void main(String[] args) {
    	port(Integer.valueOf(System.getenv("PORT")));
    	Movie m = new Movie(new int[]{18,28,80,53},7, 211, 132);
    	get("/", (request, response) -> 
    	{
    		String s = algorithm(m);
    		return "Message: "+s;
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
        	Movie movie = new Movie();
        	String baseURI = "http://api.themoviedb.org/3/movie/";
            String endURI = "?api_key=c2dcd458445148b91ed151b2a41a3c22&append_to_response=credits";
            String query =request.params(":movieId");
            query = URLEncoder.encode(query, "UTF-8");
            String URI = baseURI + query + endURI;
            JSONObject s = readJsonFromUrl(URI);
            movie.setGenres(movie.getGenres(s));
            
            return "movie: " + s.toString();
    	});
    }

	
    
    public static String algorithm(Movie m)
    {
    	String baseUri = "http://api.themoviedb.org/3/discover/movie?";

    	//request genre movies with director
    	//request genre movies with Lead actor
    	//request genre movies with similar age
    	//request genre movies with similar id
    	String genres = "with_genres="+genres(m.getGenres());
    	String years = "&year="+dates(2014);
    	String apiKey = "&api_key=c2dcd458445148b91ed151b2a41a3c22";
    	try {
			JSONObject q = readJsonFromUrl(baseUri+genres+years+apiKey);
			return q.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return baseUri+genres+years+apiKey;  	
    	//building query 
    	//year=2008&with_genres=18|28|80|53&with_people=3894
    	//score results
    	//return list of 10 best results
    	//JSONify and send to requester
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