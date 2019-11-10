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
	
	public static String[] split(String str, String lookFor, String[] notInBegin, String[] notInEnd)
	{
		if(notInBegin.length == 0)
			return str.split(lookFor);
		
		if(notInBegin.length != notInEnd.length)
			throw new IllegalArgumentException("notInBeing and notInEnd cannot differ in length!");
		
		List<String> splitList = new ArrayList<>();
		
		int[] counter = new int[notInBegin.length];
		
		StringBuilder builder = new StringBuilder();
		
		for(int i = 0; i < str.length(); i++)
		{
			if(i < str.length() - lookFor.length())
			{
				if(str.substring(i, i + lookFor.length()).equals(lookFor))
				{
					boolean canSplit = true;
					for(int j = 0; j < counter.length; j++)
					{
						if(counter[j] != 0)
						{
							canSplit = false;
							break;
						}
					}
					
					if(canSplit)
					{
						splitList.add(builder.toString());
						builder = new StringBuilder();
					}
					else
						builder.append(str.charAt(i));
				}
				else
				{
					builder.append(str.charAt(i));
					
					for(int j = 0; j < notInBegin.length; j++)
					{
						String notInB =  notInBegin[j];
						String notInE = notInEnd[j];
						
						if(i < str.length() + notInB.length())
						{
							if(str.substring(i, i + notInB.length()).equals(notInB))
							{
								boolean isValid = true;
								
								for(int l = -1; l >= -i; l++)
								{
									if(str.charAt(i + l) == '\\')
									{
										isValid = !isValid;
									}
									else
										break;
								}
								
								if(isValid)
								{
									if(notInB.equals(notInE) && counter[j] == 1)
										counter[j]--;
									else
										counter[j]++;
								}
							}
						}
						
						if(!notInB.equals(notInE) && i < str.length() + notInE.length())
						{
							if(str.substring(i, i + notInE.length()).equals(notInE))
							{
								boolean isValid = true;
								
								for(int l = -1; l >= -i; l++)
								{
									if(str.charAt(i + l) == '\\')
									{
										isValid = !isValid;
									}
									else
										break;
								}
								
								if(isValid)
								{
									counter[j]--;
									if(counter[j] < 0)
										counter[j] = 0;
								}
							}
						}
					}
				}
			}
			else
				builder.append(str.charAt(i));
		}
		
		if(!builder.toString().isEmpty())
			splitList.add(builder.toString());
		
		String[] arr = new String[splitList.size()];
		System.arraycopy(splitList.toArray(), 0, arr, 0, splitList.size());
		return arr;
	}
	
	public static String[] split(String str, String lookFor, String... notIn)
	{
		return split(str, lookFor, notIn, notIn);
	}
}
