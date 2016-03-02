package main.java;

public class Query {
	private String query;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	public void appendQuery(String queryStr)
	{
		this.query = this.query+queryStr;
	}
}
