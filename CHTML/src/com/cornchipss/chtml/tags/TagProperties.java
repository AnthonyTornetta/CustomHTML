package com.cornchipss.chtml.tags;

import com.cornchipss.chtml.tags.properties.Priority;

public final class TagProperties
{
	public static final TagProperties DEFAULT = new TagProperties(Priority.NORMAL);
	
	private Priority priority;
	
	public TagProperties(Priority p)
	{
		priority = p;
	}
	
	public Priority getPriority() { return priority; }
}
