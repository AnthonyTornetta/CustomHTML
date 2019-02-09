package com.cornchipss.chtml.util;

import java.util.Map;

public class CachedFile
{
	public CachedFile(String lines, Map<String, String> variables)
	{
		this.lines = lines;
		this.variables = variables;
	}
	
	public Map<String, String> variables;
	public String lines;
}
