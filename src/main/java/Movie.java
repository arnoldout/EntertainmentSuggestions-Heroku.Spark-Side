package main.java;

import org.json.JSONObject;

public class Movie {

	private int genres[];
	private int voteAvg;
	private int actor; 
	private int director;
    
	public Movie() {
		super();
	}

	public Movie(int[] genres, int voteAvg, int actor, int director) {
		super();
		this.genres = genres;
		this.voteAvg = voteAvg;
		this.actor = actor;
		this.director = director;
	}
	public int[] getGenres(JSONObject s) {
		int arr[] = new int[s.getJSONArray("genres").length()];
		for(int objLoop = 0; objLoop<s.getJSONArray("genres").length(); objLoop++)
		{
			arr[objLoop] = (int)s.getJSONArray("genres").getJSONObject(objLoop).get("id");
		}
		return arr;
	}
	public int[] getGenres() {
		return genres;
	}

	public void setGenres(int[] genres) {
		this.genres = genres;
	}

	public int getVoteAvg() {
		return voteAvg;
	}

	public void setVoteAvg(int voteAvg) {
		this.voteAvg = voteAvg;
	}

	public int getActor() {
		return actor;
	}

	public void setActor(int actor) {
		this.actor = actor;
	}

	public int getDirector() {
		return director;
	}

	public void setDirector(int director) {
		this.director = director;
	}
}
