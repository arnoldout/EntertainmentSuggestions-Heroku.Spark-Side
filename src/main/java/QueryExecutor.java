package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * QueryExecutor uses a string query and processes it, requesting json, and creating a list of MovieOnReturn objects from said json.
 */
public class QueryExecutor implements Runnable{
	/*
	 * query	- the get request url to theMovieDB
	 * json 	- will store the json returned
	 * movies	- list of Movies that were stored in the json
	 * reqMov	- the original movie the client used in their query
	 * isStaff	- states whether this query was built with a StaffQuery object
	 */
	private String query;
	private JSONObject json;
	private List<MovieOnReturn> movies;
	private MovieOnGet reqMov;
	private Boolean isStaff;
	
	public QueryExecutor(String query, List<MovieOnReturn> movies, MovieOnGet reqMovie, Boolean isStaff) {
		super();
		this.query = query;
		this.setMovies(movies);
		this.reqMov = reqMovie;
		this.isStaff = isStaff;
	}
	//getters and setters
	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}
	public String getQueries() {
		return query;
	}

	public void setQuery(String queries) {
		this.query = queries;
	}
	/*
	 * Overriding the run method to allow for threaded access to the code.
	 * Program will make the request to the REST server, and store the json in the json variable.
	 * Program then calls the static method in Main that will break this json down into objects, score them based on suitability
	 * 	and finally store said objects
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			this.json = readJsonFromUrl(this.query);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		Main.extractMovies(getJson(),reqMov, isStaff);
	}
	//methods to read in json
	public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
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
    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }
        return sb.toString();
      }
	public List<MovieOnReturn> getMovies() {
		return movies;
	}
	public void setMovies(List<MovieOnReturn> movies) {
		this.movies = movies;
	}
	
}
