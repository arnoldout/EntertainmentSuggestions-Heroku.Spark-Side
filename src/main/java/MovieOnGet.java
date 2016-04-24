package main.java;

import org.json.JSONArray;
import org.json.JSONObject;
/*
 * MovieOnGet stores the json returned from the first request. I.E. the information about the movie searched for by the client. 
 * The MovieOnGet stores lots of information about the search movie, and is used heavily for comparison when scoring. 
 */
public class MovieOnGet {
	
	/*
	 * id		- The id provided by theMovieDB
	 * genres	- A collection of genre ids associated with the movie
	 * keywords	- A collection of keyword ids associated with the movie
	 * voteAvg	- The score out of 10 given by users on average. Relates to how good the movie is.
	 * actors	- A collection of actor ids associated with the movie
	 * director	- The id of the director for the movie
	 * writer	- The id of the writer for the movie
	 */
	private int id;
	private int genres[];
	private int keywords[];
	private double voteAvg;
	private int actors[]; 
	private int director;
    private int writer;
	
	public MovieOnGet(JSONObject json, String genreStr)
	{
		this.id = this.getId(json);
		this.genres = this.getGenres(json, genreStr);
		this.voteAvg = this.getVoteAvg(json);
		this.actors = this.getActor(json);
		this.director = this.getDirector(json);
		this.writer = this.getWriter(json);
		this.keywords = this.getKeywords(json, "keywords");
	}
	public int getWriter() {
		return writer;
	}
	public void setWriter(int writer) {
		this.writer = writer;
	}
	public int getId(JSONObject s) {
		return s.getInt("id");
	}
	
	//extract the genre ids from the json object
	public int[] getGenres(JSONObject movieJson, String queryString) {
		int arr[] = new int[movieJson.getJSONArray(queryString).length()];
		for(int objLoop = 0; objLoop<movieJson.getJSONArray(queryString).length(); objLoop++)
		{
			arr[objLoop] = (int)movieJson.getJSONArray(queryString).getJSONObject(objLoop).get("id");		
		}
		return arr;
	}
	//extract the keyword ids from the json object
	public int[] getKeywords(JSONObject movieJson, String queryString) {
		JSONObject k = (JSONObject) movieJson.get(queryString);
		int arr[] = new int[k.getJSONArray(queryString).length()];
		for(int objLoop = 0; objLoop<k.getJSONArray(queryString).length(); objLoop++)
		{
			arr[objLoop] = (int)k.getJSONArray(queryString).getJSONObject(objLoop).get("id");		
		}
		return arr;
	}
	//extract the average vote from the json object
	public double getVoteAvg(JSONObject movieJson) {
		return movieJson.getDouble("vote_average");
	}
	//extract the actor ids from the json object
	public int[] getActor(JSONObject movieJson)
	{
		int[] actors = new int[1];
		JSONObject castArr = (JSONObject) movieJson.get("credits");
		for(int actorLoop = 0; actorLoop<actors.length; actorLoop++)
		{
			JSONObject castObj = (JSONObject)castArr.getJSONArray("cast").get(actorLoop);
			actors[actorLoop] = (int)castObj.get("id");
		}
		return actors;
	}
	//extract the director id from the json object
	public int getDirector(JSONObject movieJson) {
		
		JSONObject castArr = (JSONObject) movieJson.get("credits");
		JSONArray c = (JSONArray)castArr.getJSONArray("crew");
		for(int objLoop = 0; objLoop<c.length(); objLoop++)
		{
			if(((String)c.getJSONObject(objLoop).get("department")).equals("Directing"))
			{
				return (int) c.getJSONObject(objLoop).get("id");
			}
		}
		return -1;
	}
	//extract the writer id from the json object
	public int getWriter(JSONObject movieJson) {
			
			JSONObject castArr = (JSONObject) movieJson.get("credits");
			JSONArray c = (JSONArray)castArr.getJSONArray("crew");
			for(int objLoop = 0; objLoop<c.length(); objLoop++)
			{
				if(((String)c.getJSONObject(objLoop).get("department")).equals("Writing"))
				{
					return (int) c.getJSONObject(objLoop).get("id");
				}
			}
			return -1;
	}
	//getters and setters
	public int[] getGenres() {
		return genres;
	}
	public int getGenres(int place) {
		return genres[place];
	}

	public void setGenres(int[] genres) {
		this.genres = genres;
	}

	public double getVoteAvg() {
		return voteAvg;
	}

	public void setVoteAvg(double voteAvg) {
		this.voteAvg = voteAvg;
	}

	public int getDirector() {
		return director;
	}

	public void setDirector(int director) {
		this.director = director;
	}
	public int[] getActors() {
		return actors;
	}
	public void setActors(int actors[]) {
		this.actors = actors;
	}
	public String getActors(char c) {
		StringBuilder sb = new StringBuilder();
		sb.append(actors[0]);
		for(int actorLoop = 1; actorLoop<actors.length; actorLoop++)
		{
			sb.append(""+c+actors[actorLoop]);
		}
		return sb.toString();
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int[] getKeywords() {
		return keywords;
	}
	public void setKeywords(int keywords[]) {
		this.keywords = keywords;
	}
}
