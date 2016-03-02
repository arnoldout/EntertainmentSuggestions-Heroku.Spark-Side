package main.java;

public class QueryBuilder {

	private Query queries[];
	public QueryBuilder(Movie m)
	{
		this.queries = new Query[3];
		queries = initQueries(m.getGenres(), queries);
		queries = addStmntQueries(queries, m);
		queries = appendAPIKey(queries, "&api_key=c2dcd458445148b91ed151b2a41a3c22");
	}
	
	public Query[] getQueries() {
		return queries;
	}

	public void setQueries(Query[] queries) {
		this.queries = queries;
	}

	private Query[] appendAPIKey(Query[] queries, String APIKey) 
	{
		for(int queryLoop = 0; queryLoop<queries.length; queryLoop++)
		{
			queries[queryLoop].appendQuery(queries[queryLoop].getQuery()+APIKey);
		}
		return queries;
	}
	public Query[] addStmntQueries(Query[] queries, Movie m)
	{
		for(int queryLoop = 0; queryLoop < queries.length; queryLoop++)
		{
			switch(queryLoop)
			{
			case 0:
				queries[queryLoop].appendQuery(queries[queryLoop].getQuery()+"&with_cast="+m.getActor());
				break;
			case 1:
				queries[queryLoop].appendQuery(queries[queryLoop]+"&with_crew="+m.getDirector());
				break;
			case 2:
				queries[queryLoop].appendQuery(queries[queryLoop]+"&vote_average.gte="+m.getVoteAvg());
				break;
			}
		}
		return queries;
	}
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
	public Query[] initQueries(int[] genres, Query queries[])
    {
    	for(int queryLoop = 0; queryLoop<queries.length; queryLoop++)
    	{
    		queries[queryLoop].setQuery(initQuery(queries[queryLoop])+"with_genres="+generateGenres(genres)); 
    	}
    	return queries;
    }
	public String initQuery(Query query)
	{
		return "http://api.themoviedb.org/3/discover/movie?";
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
