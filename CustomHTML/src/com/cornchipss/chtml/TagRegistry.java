package com.cornchipss.chtml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cornchipss.chtml.tags.ICustomTag;
import com.cornchipss.chtml.tags.TagProperties;

public final class TagRegistry
{
	private static List<ICustomTag> tags = new ArrayList<>();
	private static Map<ICustomTag, TagProperties> properties = new HashMap<>();
	
	/**
	 * Registers a CustomHTML tag with the default tag properties
	 * @param t The tag to register
	 */
	public static void registerTag(ICustomTag t)
	{
		registerTag(t, TagProperties.DEFAULT);
	}
	
	/**
	 * Registers a CustomHTML tag with custom TagProperties
	 * @param t The tag to register
	 * @param p The properties to apply
	 */
	public static void registerTag(ICustomTag t, TagProperties p)
	{
		if(properties.containsKey(t))
		{
			if(properties.get(t).getPriority().compareTo(p.getPriority()) <= 0)
				properties.put(t, p); // If the tag has a bigger or same priority, override the old tag
		}
		else
			properties.put(t, p);
	}
	
	/**
	 * Puts all the tags that are present in the tag list
	 */
	public static void init()
	{
		tags.clear();
		tags.addAll(properties.keySet());
	}
	
	// Getters & Setters //
	public static List<ICustomTag> getTags() { return tags; }
	public static TagProperties getProperties(ICustomTag t) { return properties.get(t); }
}
