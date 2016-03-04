package main.java;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

public class MovieOnGet {

	private int genres[];
	private double voteAvg;
	private int actors[]; 
	private int director;
    
	public MovieOnGet() {
		super();
	}
	public MovieOnGet(JSONObject json, String genreStr)
	{
		this.genres = this.getGenres(json, genreStr);
		this.voteAvg = this.getVoteAvg(json);
		this.actors = this.getActor(json);
		this.director = this.getDirector(json);
	}
	public MovieOnGet(int[] genres, double voteAvg, int actor[], int director) {
		super();
		this.genres = genres;
		this.voteAvg = voteAvg;
		this.actors = actor;
		this.director = director;
	}
	public int[] getGenres(JSONObject s, String queryString) {
		int arr[] = new int[s.getJSONArray(queryString).length()];
		for(int objLoop = 0; objLoop<s.getJSONArray(queryString).length(); objLoop++)
		{
			arr[objLoop] = (int)s.getJSONArray(queryString).getJSONObject(objLoop).get("id");
			/*JSONArray jsk = s.getJSONArray(queryString);
			JSONObject nnmn = jsk.getJSONObject(objLoop);//.get("id");
			int i = (int)jsk.get("id");
			i = i+1;*/
			
		}
		return arr;
	}
	public double getVoteAvg(JSONObject s) {
		return s.getDouble("vote_average");
	}
	
	public int[] getActor(JSONObject s)
	{
		int[] actors = new int[2];
		JSONObject castArr = (JSONObject) s.get("credits");
		for(int actorLoop = 0; actorLoop<actors.length; actorLoop++)
		{
			JSONObject castObj = (JSONObject)castArr.getJSONArray("cast").get(actorLoop);
			actors[actorLoop] = (int)castObj.get("id");
		}
		return actors;
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
}
