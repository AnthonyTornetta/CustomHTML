package com.cornchipss.chtml.util;

import java.util.HashMap;
import java.util.Map;

import com.cornchipss.chtml.bexp.OutsidePattern;

public class TagUtils
{
	public static String stripTag(String tag)
	{
		return tag;
	}
	
	public static boolean isClosingTag(String tag)
	{
		return tag.charAt(0) == '/';
	}
	
	public static String getName(String tag)
	{
		int index = tag.indexOf(' ');
		return (index != -1 ? tag.substring(0, index) : tag).replace("\n", "");
	}

	public static Map<String, String> getAttributes(String tagString, String name)
	{
		String attributesString = Helper.removeTrailingWhiteSpace(tagString.substring(name.length(), tagString.length()));
		
		Map<String, String> attributes = new HashMap<>();
		
		if(attributesString.length() != 0)
		{
			if(attributesString.charAt(attributesString.length() - 1) == '/')
			{
				attributesString = Helper.removeTrailingWhiteSpace(attributesString.substring(0, attributesString.length() - 1));
			}

			System.out.println(attributesString);
			
			if(attributesString.length() != 0)
			{
				String[] attrsSplitSpace = OutsidePattern.split(attributesString, " ", "\"", "'");
				
				for(String attrLine : attrsSplitSpace)
				{
					System.out.println(attrLine);
					
					if(attrLine.length() == 0)
						continue; // It's empty meaning there was an empty space
					
					String[] splitEquals = OutsidePattern.split(attrLine, "=", "\"", "'");

					if(splitEquals.length > 1) // for full attributes (something="asdf")
					{
						int quoteIndexBegin = splitEquals[1].indexOf("\"");
						int quoteIndexEnd;
						if(quoteIndexBegin == -1)
						{
							quoteIndexBegin = splitEquals[1].indexOf("'");
							quoteIndexEnd = splitEquals[1].lastIndexOf("'");
						}
						else
						{
							quoteIndexEnd = splitEquals[1].lastIndexOf("\"");
						}

						attributes.put(splitEquals[0], splitEquals[1].substring(quoteIndexBegin + 1, quoteIndexEnd)); // Removes the ""s around the attribute
					}
					else if(splitEquals.length == 1) // There are some attributes w/ only a title and no value (e.g. download in the <a> tag), so handle those differently
					{
						attributes.put(splitEquals[0], ""); // Give it the value of an empty String
					}
					else
					{
						continue; // A line that is being skipped has been hit and has no characters after it

						/*
						 * This has been shown to happen during multiline comments that begin like this:
						 * <!--
						 * ...
						 * -->
						 * With no whitespace after the comment opening
						 */
					}
				}
			}
		}
		
		return attributes;
	}
}
