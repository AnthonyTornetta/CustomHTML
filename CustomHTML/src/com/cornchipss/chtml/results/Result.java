package com.cornchipss.chtml.results;

public class Result
{
	private String repWith;
	private int start, end;
	
	/**
	 * <p>Determines how the resulting HTML will be modified</p>
	 * <p>All text between and including the start and end will be replaced with the first argument</p>
	 * @param repWith What the text should be replaced with
	 * @param start The starting area to insert the text
	 * @param end The ending area to insert the text
	 */
	public Result(String repWith, int start, int end)
	{
		this.repWith = repWith;
		this.start = start;
		this.end = end;
	}

	public String getRepWith() { return repWith; }
	public int getStart() { return start; }
	public int getEnd() { return end; }
}
