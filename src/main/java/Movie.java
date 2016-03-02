package main.java;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

public class Movie {

	private int genres[];
	private double voteAvg;
	private int actor; 
	private int director;
    
	public Movie() {
		super();
	}

	public Movie(int[] genres, double voteAvg, int actor, int director) {
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
	public double getVoteAvg(JSONObject s) {
		return s.getDouble("vote_average");
	}
	
	public int getActor(JSONObject s) {
		
		JSONObject castArr = (JSONObject) s.get("credits");
		JSONObject c = (JSONObject)castArr.getJSONArray("cast").get(0);
		return (int)c.get("id");
	}
	public int getDirector(JSONObject s) {
		
		JSONObject castArr = (JSONObject) s.get("credits");
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
	
	@Override
	public String toString() {
		return "Movie [genres=" + Arrays.toString(genres) + ", voteAvg=" + voteAvg + ", actor=" + actor + ", director="
				+ director + "]";
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
