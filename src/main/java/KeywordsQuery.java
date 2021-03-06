package main.java;

/*
 * KeywordsQuery builds up a get request to send to theMovieDB server, and will return all movies with the provided keywords
 */
public class KeywordsQuery extends QueryBuilder {

	/*
	 * query	- The request to be sent to theMovieDB
	 * movie	- The original movie used as a parameter from the client
	 * pageNo	- The specific page of JSON needed 	
	 */
	private String query;
	private MovieOnGet movie;
	private int pageNo;
	//constructor
	public KeywordsQuery(MovieOnGet movie, int pageNo)
	{
		super(movie, pageNo);
		this.query = new String();
		this.movie = movie;
		this.setPageNo(pageNo);
	}
	//create queries pools the data together to build up a query for theMovieDB
	public void createQueries()
	{
		this.query = initQueries(movie.getKeywords(), query);
		this.query = addStmntQueries(query, movie);
		this.query = appendAPIKey(query, "&api_key=c2dcd458445148b91ed151b2a41a3c22");
	}

	/*
	 * These few methods are used to add various information to the query, and in the required format
	 */
	private String appendAPIKey(String query, String APIKey) 
	{
		return ((query+APIKey));
	}
	public String addStmntQueries(String query, MovieOnGet m)
	{
		return (query+"&vote_count.gte="+50+"&page="+getPageNo());
	}
	
	public String generateKeywords(int[] keywords)
    {
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0; i<keywords.length; i++)
    	{
    		if(i!=0)
    		{
    			sb.append("|");
    		}
    		sb.append(keywords[i]);
    	}
    	return sb.toString();
    }
	public String initQueries(int[] keywords, String query)
    {
		return "http://api.themoviedb.org/3/discover/movie?"+"with_keywords="+generateKeywords(keywords)+"&page="+getPageNo(); 
    }
	public int getPageNo() {
		return this.pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public String getQueries() {
		return query;
	}

	public void setQueries(String query) {
		this.query = query;
	}

}
