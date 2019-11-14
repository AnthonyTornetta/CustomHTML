package com.cornchipss.chtml.util;

public class HTMLUtils
{
	/**
	 * Finds the closing tag and returns the beginning index of the closing tag at the returned array index of 0, and the end tag's index at the returned array's index of 1
	 * @param closingTag The closing tag to search for
	 * @param lines The lines to search for the closing tag in
	 * @param startLookingAt The index to start the search at (after the initial opening tag)
	 * @return The beginning index of the closing tag at the returned array index of 0, and the end tag's index at the returned array's index of 1
	 */
	public static int[] findClosingTag(String closingTag, String lines, int startLookingAt)
	{
		int start = -1;

		for(int i = startLookingAt; i + closingTag.length() <= lines.length(); i++)
		{
			if(lines.substring(i, i + closingTag.length()).equalsIgnoreCase(closingTag))
			{
				start = i;
				break;
			}
		}
		
		if(start == -1)
			return new int[] { -1, -1 };
		else
			return new int[] { start, start + closingTag.length() };
	}

	public static int[] findNextTag(String lines, int startSearch)
	{
		if(startSearch + 1 < lines.length() - 1)
		{
			String substring = lines.substring(startSearch + 1);

			int chunkOfLineBefore = lines.indexOf(substring);

			int tempStart = substring.indexOf("<") + chunkOfLineBefore;
			int tempEnd = substring.indexOf(">") + chunkOfLineBefore;

			if(tempStart > startSearch && tempEnd > startSearch)
			{
				return new int[] { tempStart, tempEnd };
			}
			else
			{
				return new int[] { -1, -1 };
			}
		}
		else
		{
			return new int[] { -1, -1 };
		}
	}
}
