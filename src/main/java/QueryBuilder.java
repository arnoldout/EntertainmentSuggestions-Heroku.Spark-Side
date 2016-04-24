package main.java;

/*
 * Abstract class that defines variables for sub classes to use
 * 	Sub classes are the KeywordsQuery class and the StaffQuery class.
 */
public abstract class QueryBuilder {

	private String query;
	//MovieOnGet refers to the movie requested by the client
	private MovieOnGet movie;
	private int pageNo;
	//constructor
	public QueryBuilder(MovieOnGet movie, int pageNo)
	{
		this.movie = movie;
		this.pageNo = pageNo;
	}
	public abstract void createQueries();
	
	public abstract String getQueries();
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public MovieOnGet getMovie() {
		return movie;
	}
	public void setMovie(MovieOnGet movie) {
		this.movie = movie;
	}
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
}