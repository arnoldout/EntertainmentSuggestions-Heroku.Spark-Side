package main.java;

import org.json.JSONObject;

/*
 * MovieOnReturn stores any potential movies that could be returned the client.
 * 	It also stores the whole json object of that particular movie, which is used as an easy way to gather all the json objects for returning movies.
 */
public class MovieOnReturn {
	
	private int id;
	private int genres[];
	private double voteAvg;
	private JSONObject json;
	private int score;
	private boolean isStaff;
    
	//constructors
	public MovieOnReturn() {
		super();
	}
	
	public MovieOnReturn(JSONObject json, String genreStr, Boolean isStaff)
	{
		this.id = this.getId(json);
		this.genres = this.getGenres(json, genreStr);
		this.json = json;
		this.isStaff = isStaff;
	}
	
	public MovieOnReturn(int[] genres, double voteAvg) {
		super();
		this.genres = genres;
		this.voteAvg = voteAvg;
	}
	//getters and setters
	public boolean isStaff() {
		return isStaff;
	}
	public void setStaff(boolean isStaff) {
		this.isStaff = isStaff;
	}
	
	public int getId(JSONObject s) {
		return s.getInt("id");
	}
	public JSONObject getJson() {
		return json;
	}
	public void setJson(JSONObject json) {
		this.json = json;
	}

	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public void appendScore(int score) {
		this.score += score;
	}
	
	public int[] getGenres(JSONObject s, String queryString) {
		int[] k = new int[s.getJSONArray(queryString).length()];
		int arr[] = new int[k.length];
		for(int objLoop = 0; objLoop<k.length; objLoop++)
		{		
			arr[objLoop] = (int)s.getJSONArray(queryString).getInt(objLoop);			
		}
		return arr;
	}
	public int getGenres(int place) {
		return genres[place];
	}
	
	public double getVoteAvg(JSONObject s) {
		return s.getDouble("vote_average");
	}
	
	public int[] getGenres() {
		return genres;
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
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
