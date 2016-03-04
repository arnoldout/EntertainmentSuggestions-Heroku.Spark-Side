package main.java;

public class QueryBuilder {

	private String query;
	private MovieOnGet movie;
	public QueryBuilder(MovieOnGet movie)
	{
		this.query = new String();
		this.movie = movie;
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
		return (query+"&with_people="+m.getActors('|')+"|"+m.getDirector());
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
	/*
    public static String dates(int year)
    {
    	StringBuilder sb = new StringBuilder();
    	int strtYear = year-5;;
    	int yrDiff = Calendar.getInstance().get(Calendar.YEAR)-year;
    	if(yrDiff<5)
		{
    		strtYear = strtYear - yrDiff;
		}
    	for(int yrLoop = 0; yrLoop<10; yrLoop++)
    	{
    		if(yrLoop!=0)
    		{
    			sb.append("|");
    		}
    		int currYear = strtYear+yrLoop;
    		sb.append(currYear);
    	}
    	return sb.toString();
    }*/
}
