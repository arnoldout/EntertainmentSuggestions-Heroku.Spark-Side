package main.java;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * The QueryNameFinder is used to refine json search queries.
 * TheMovieDB is open for anyone to insert or edit data. Because of this, there can be movies that were never made, 
 * 	some movies can be entered multiple times, and some movies can have incorrect data. Usually these movies are only 
 * 	edited or added by a few people, and so have a low vote-Count value. When the run method is called, the json page
 * 	is refined down to movies where at least 25 people have verified that the data is accurate. I found that for 90% of 
 * 	cases, this is a high enough value to remove most dud entries. 
 */
public class QueryNameFinder implements Runnable{
	/*
	 *  movieList	- The page of json that contains multiple movie objects
	 *  returnJSON	- The page of json with dud movies removed
	 */
	private JSONArray movieList;
	private JSONArray returnJSON;
	//constructor
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
			//if the movie has a vote_count of more than 25, the movie is added to returnJSON
			if(voteCount>25)
			{
				//good result
				returnJSON.put(js);
			}
		}
	}
	//getters and setters
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
