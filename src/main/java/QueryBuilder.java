package main.java;

public class QueryBuilder {

	private String query;
	private MovieOnGet movie;
	private int pageNo;
	public QueryBuilder(MovieOnGet movie, int pageNo)
	{
		this.query = new String();
		this.movie = movie;
		this.setPageNo(pageNo);
	}
	public void createQueries()
	{
		this.query = initQueries(movie.getGenres(), query);
		this.query = addStmntQueries(query, movie);
		this.query = appendAPIKey(query, "&api_key=c2dcd458445148b91ed151b2a41a3c22");
	}
	public String getQueries() {
		return query;
	}

	public void setQueries(String query) {
		this.query = query;
	}

	private String appendAPIKey(String query, String APIKey) 
	{
		return ((query+APIKey));
	}
	public String addStmntQueries(String query, MovieOnGet m)
	{
		return (query+"&with_people="+m.getActors('|')+"|"+m.getDirector()+"&page="+getPageNo());
	}
	//error heres
	public String generateGenres(int[] genres)
    {
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0; i<genres.length; i++)
    	{
    		if(i!=0)
    		{
    			sb.append("|");
    		}
    		sb.append(genres[i]);
    	}
    	return sb.toString();
    }
	public String initQueries(int[] genres, String query)
    {
		return "http://api.themoviedb.org/3/discover/movie?"+"with_genres="+generateGenres(genres); 
    }
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
}