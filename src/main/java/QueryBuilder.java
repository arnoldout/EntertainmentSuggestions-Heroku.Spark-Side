package main.java;

import java.util.Calendar;

public class QueryBuilder {

	/*
	 * 
	 * DON'T LIKE SCOPE AT THE MOMENT
	 * CHANGE LATER
	 * 
	 */
	public static String genres(int[] genres)
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
    }
}
