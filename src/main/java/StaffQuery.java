package main.java;
/*
 * The staff Query class makes a get request to theMovieDB for movies that have the same members of staff as the client's requested movie. 
 */
public class StaffQuery extends QueryBuilder{

	/*
	 * 	movie	- the movie the client originally requested
	 * 	pageNo	- the page of json this query needs to get from the server
	 */
	public StaffQuery(MovieOnGet movie, int pageNo) {
		super(movie, pageNo);
		// TODO Auto-generated constructor stub
	}

	//Build the query
	@Override
	public void createQueries() {
		// TODO Auto-generated method stub
		super.setQuery(initQueries(super.getQuery()));
		super.setQuery(addStmntQueries(super.getQuery(), super.getMovie()));
		super.setQuery(appendAPIKey(super.getQuery(), "&api_key=c2dcd458445148b91ed151b2a41a3c22"));
	}
	public String initQueries(String query)
    {
		//first part of request is built
		return "http://api.themoviedb.org/3/discover/movie?"+"with_people="+getStaff()+"&page="+getPageNo(); 
    }
	
	public String getStaff()
	{
		//the get request needs as many staff ids that will be used
		StringBuilder sb = new StringBuilder();
		sb.append(getActors(super.getMovie().getActors()));
		sb.append(getDirector(super.getMovie().getDirector()));
		sb.append(super.getMovie().getWriter());
		return sb.toString();
	}
	public String getDirector(int id)
	{
		//between ids, there needs to be a '|' symbol to tell the server, the staff are optional
		//this symbol means "OR"
		return (id+"|");
	}
	public String getActors(int[] actors)
	{
		//build a string with a bar separating, meaning OR
		StringBuilder sb = new StringBuilder();
		for(int actorLoop = 0; actorLoop<actors.length; actorLoop++)
		{
			sb.append(actors[actorLoop]);
			sb.append("|");
		}
		return sb.toString();
	}
	//append my API key to the end to gain access to the server
	private String appendAPIKey(String query, String APIKey) 
	{
		return ((query+APIKey));
	}

	@Override
	public String getQueries() {
		// TODO Auto-generated method stub
		return super.getQuery();
	}
	//add some extra parameteres, i.e. ensure that movies can only be added when they have more than 50 votes
	public String addStmntQueries(String query, MovieOnGet m)
	{
		return (query+"&vote_count.gte="+50+"&page="+getPageNo());
	}

	

}
