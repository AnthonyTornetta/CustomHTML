package com.cornchipss.chtml.results;

public class ReplaceResult
{
	private String repWith;
	private int start, fin;
	
	public ReplaceResult(String repWith, int start, int fin)
	{
		this.repWith = repWith;
		this.start = start;
		this.fin = fin;
	}

	public String getRepWith() { return repWith; }
	public int getStart() { return start; }
	public int getEnd() { return fin; }
}
