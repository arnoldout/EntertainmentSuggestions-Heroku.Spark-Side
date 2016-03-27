package main.java;

public class StaffQuery extends QueryBuilder{

	public StaffQuery(MovieOnGet movie, int pageNo) {
		super(movie, pageNo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createQueries() {
		// TODO Auto-generated method stub
		super.setQuery(initQueries(super.getMovie().getKeywords(), super.getQuery()));
		super.setQuery(addStmntQueries(super.getQuery(), super.getMovie()));
		super.setQuery(appendAPIKey(super.getQuery(), "&api_key=c2dcd458445148b91ed151b2a41a3c22"));
	}
	public String initQueries(int[] keywords, String query)
    {
		return "http://api.themoviedb.org/3/discover/movie?"+"with_people="+getStaff()+"&page="+getPageNo(); 
    }
	
	public String getStaff()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getActors(super.getMovie().getActors()));
		sb.append(getDirector(super.getMovie().getDirector()));
		sb.append(super.getMovie().getWriter());
		return sb.toString();
	}
	public String getDirector(int id)
	{
		return (id+"|");
	}
	public String getActors(int[] actors)
	{
		StringBuilder sb = new StringBuilder();
		for(int actorLoop = 0; actorLoop<actors.length; actorLoop++)
		{
			sb.append(actors[actorLoop]);
			sb.append("|");
		}
		return sb.toString();
	}
	private String appendAPIKey(String query, String APIKey) 
	{
		return ((query+APIKey));
	}

	@Override
	public String getQueries() {
		// TODO Auto-generated method stub
		return super.getQuery();
	}
	public String addStmntQueries(String query, MovieOnGet m)
	{
		return (query+"&vote_count.gte="+50+"&page="+getPageNo());
	}

	

}
