package com.cornchipss.chtml.bexp;

import java.util.ArrayList;
import java.util.List;

public class OutsidePattern
{
	public static int indexOf(String str, String lookFor, String notInBegin, String notInEnd)
	{
		boolean ignoreKey = false;
		boolean ignoring = false;
		
		boolean keysSame = notInBegin.equals(notInEnd);
		
		for(int i = 0; i + lookFor.length() <= str.length(); i++)
		{
			if(str.charAt(i) == '\\')
				ignoreKey = !ignoreKey;
			
			if(!ignoreKey)
			{
				if(i + notInEnd.length() <= str.length())
				{
					if(str.substring(i, notInEnd.length() + i).equals(notInEnd))
					{
						if(keysSame)
						{
							ignoring = !ignoring;
						}
						else
						{
							ignoring = false;
						}
					}
				}
				
				if(!keysSame && i + notInBegin.length() <= str.length())
				{
					if(str.substring(i, notInBegin.length() + i).equals(notInBegin))
					{
						ignoring = true;
					}
				}
			}
			
			if(!ignoring && str.substring(i, lookFor.length() + i).equals(lookFor))
				return i;
		}
		
		return -1;
	}
	
	public static int indexOf(String str, String lookFor, String notIn)
	{
		return indexOf(str, lookFor, notIn, notIn);
	}
	
	public static String[] split(String str, String lookFor, String notInBegin, String notInEnd)
	{		
		List<String> splitList = new ArrayList<>();
		
		int index = indexOf(str, lookFor, notInBegin, notInEnd);
		int lastIndex = -1;
		
		while(index != lastIndex && index + 1 != str.length())
		{
			splitList.add(str.substring(lastIndex + 1, index));
			
			lastIndex = index;
			
			index = index + 1 + OutsidePattern.indexOf(str.substring(index + 1), lookFor, notInBegin, notInEnd);
		}
		
		if(lastIndex + 1 != str.length())
			splitList.add(str.substring(lastIndex + 1));
		
		String[] arr = new String[splitList.size()];
		for(int i = 0; i < arr.length; i++)
		{
			arr[i] = splitList.get(i);
		}
		
		return arr;
	}
	
	public static String[] split(String str, String lookFor, String notIn)
	{
		return split(str, lookFor, notIn, notIn);
	}
}
