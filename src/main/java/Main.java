package main.java;

import static spark.Spark.*;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

import org.json.*;

public class Main {
    public static void main(String[] args) {
    	port(Integer.valueOf(System.getenv("PORT")));
    	get("/", (request, response) -> {return "Message";});
        get("/srch/movie/:movieName", (request, response) -> {
        	String baseURI = "http://api.themoviedb.org/3";
            String srch = "/search/movie?query=";
            String apiKey = "api_key=c2dcd458445148b91ed151b2a41a3c22";
            String query =request.params(":movieName");
            query = URLEncoder.encode(query, "UTF-8");
            String URI = baseURI + srch + query + "&" + apiKey;
            JSONObject s = readJsonFromUrl(URI);
            return "movie: " + s.toString();
        });

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