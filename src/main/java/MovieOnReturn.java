package main.java;

import org.json.JSONObject;

public class MovieOnReturn {
	
	private int id;
	private int genres[];
	private double voteAvg;
	private JSONObject json;
	private int score;
	private boolean isScored;
    
	public MovieOnReturn() {
		super();
	}
	public MovieOnReturn(JSONObject json, String genreStr)
	{
		this.id = this.getId(json);
		this.genres = this.getGenres(json, genreStr);
		this.voteAvg = this.getVoteAvg(json);
		this.json = json;
	}
	public MovieOnReturn(int[] genres, double voteAvg) {
		super();
		this.genres = genres;
		this.voteAvg = voteAvg;
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
	public boolean isScored() {
		return isScored;
	}
	public void setScored(boolean isScored) {
		this.isScored = isScored;
	}
	public int[] getGenres(JSONObject s, String queryString) {
		int l = 5;
		int[] k = new int[s.getJSONArray(queryString).length()];
		int arr[] = new int[l];//new int[s.getJSONArray(queryString).length()];
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
