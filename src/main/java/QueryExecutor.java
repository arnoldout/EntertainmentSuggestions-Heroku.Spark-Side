package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class QueryExecutor implements Runnable{

	private Query query;
	private ConcurrentHashMap<Integer, JSONObject> jsonCollection;
	
	
	public QueryExecutor(Query query, ConcurrentHashMap<Integer, JSONObject> jsonCollection) {
		super();
		this.query = query;
		this.jsonCollection = jsonCollection;
	}
	

	public Query getQueries() {
		return query;
	}


	public void setQuery(Query queries) {
		this.query = queries;
	}

	public ConcurrentHashMap<Integer, JSONObject> getJsonCollection() {
		return jsonCollection;
	}


	public void setJsonCollection(ConcurrentHashMap<Integer, JSONObject> jsonCollection) {
		this.jsonCollection = jsonCollection;
	}


	//overriding the run method from runnable
	public void run() {
		try {
			JSONObject s = readJsonFromUrl(this.query.getQuery());
			System.out.println(s.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	
}
