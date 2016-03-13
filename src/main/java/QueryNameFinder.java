package main.java;

import org.json.JSONArray;
import org.json.JSONObject;

public class QueryNameFinder implements Runnable{

	private JSONArray movieList;
	private JSONArray returnJSON;
	
	public QueryNameFinder(JSONArray json, JSONArray returnJson) {
		super();
		this.movieList = json;
		this.returnJSON = returnJson;
	}
	
	public void run()
	{	
		for(int movieLoop = 0; movieLoop<movieList.length(); movieLoop++)
		{
			JSONObject js = movieList.getJSONObject(movieLoop);
			int voteCount = (int) js.get("vote_count");

			if(voteCount>25)
			{
				//good result
				returnJSON.put(js);
			}
		}
	}
	public JSONArray getJson() {
		return movieList;
	}
	public void setJson(JSONArray json) {
		this.movieList = json;
	}
	
	public JSONArray getReturnJSON() {
		return returnJSON;
	}

	public void setReturnJSON(JSONArray returnJSON) {
		this.returnJSON = returnJSON;
	}

	
}
