package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QueryExecutor implements Runnable{

	private String query;
	private JSONObject json;
	private List<MovieOnReturn> movies;
	private MovieOnGet reqMov;
	
	public QueryExecutor(String query, List<MovieOnReturn> movies, MovieOnGet reqMovie) {
		super();
		this.query = query;
		this.setMovies(movies);
		this.reqMov = reqMovie;
	}
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
		Main.lister(getJson(),reqMov);
	}
	public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
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
