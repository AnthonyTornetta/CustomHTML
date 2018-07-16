package com.cornchipss.chtml.util;

import java.util.ArrayList;
import java.util.List;

public class Helper 
{
	public static boolean isWhole(double num)
	{
		return (num % 1) == 0;
	}
	
	public static boolean isWhole(Number x)
	{
		return isWhole(x.doubleValue());
	}
	
	public static int sign(double num)
	{
		return (int) (num / Math.abs(num));
	}
	
	public static double clamp(double val, double min, double max)
	{
		if(val < min)
			val = min;
		if(val > max)
			val = max;
		return val;
	}
	
	public static String pretty(double d)
	{
		if((int)d == d)
			return (int)d + "";
		return d + "";
	}
	
	public static boolean isInt(String possibleNumber)
	{
		boolean validNumber = false; // We use this to see if the last number checked was a number too
		
		for(int i = 0; i < possibleNumber.length(); i++)
		{
			char checking = possibleNumber.charAt(i);
			
			if(checking == '-' || checking == '+')
			{
				if(possibleNumber.length() == i + 1) // All the number is is '-' or '+'
					return false;
				if(validNumber) // There were already numbers found
					return false;
			}
			else
			{
				for(char j = '0'; j <= '9'; j++)
				{
					if(checking == j)
					{
						validNumber = true;
					}
				}
				if(!validNumber)
					return false;
			}
		}
		return true; // We only got here if no exception was found and it is a number
	}
	
	public static boolean isDouble(String possibleDouble)
	{
		boolean decimalFound = false;
		boolean validNumber = false;
		
		for(int i = 0; i < possibleDouble.length(); i++)
		{
			char checking = possibleDouble.charAt(i);
			if(checking == ' ')
				return false;
			
			if(checking == '.')
			{
				if(decimalFound)
					return false; // 2 decimals found
				decimalFound = true;
			}
			else if(checking == '-' || checking == '+')
			{
				if(possibleDouble.length() == i + 1) // All the number is is '-'
					return false;
				if(validNumber || decimalFound) // There were already numbers found
					return false;
			}
			else if(!(checking >= '0' && checking <= '9'))
					return false;
		}
		return true; // We only got here if no exception was found and it is a number
	}
	
	public static String[] getStringsBetween(String str, String open, String close)
	{
		int betweenIndex = 0;
		List<String> betweens = new ArrayList<>();;
		boolean copying = false;
		
		for(int i = 0; i < str.length(); i++)
		{
			String lookAt;
			
			if(i + open.length() <= str.length())
			{
				lookAt = str.substring(i, open.length());
				if(lookAt.equals(open))
				{
					int ecapeSequences = 0;
					for(int j = i - 1; j >= 0; j--)
					{
						if(lookAt.charAt(j) == '\\')
							ecapeSequences++;
					}
					if(ecapeSequences % 2 == 1)
						copying = true;
				}
			}
			
			lookAt = str.substring(i, close.length());
			if(lookAt.equals(close))
			{
				int ecapeSequences = 0;
				for(int j = i - 1; j >= 0; j--)
				{
					if(lookAt.charAt(j) == '\\')
						ecapeSequences++;
				}
				if(ecapeSequences % 2 == 1)
				{
					betweenIndex++;
					copying = false;
				}
			}
			
			if(copying)
			{
				if(betweens.size() <= betweenIndex)
					betweens.set(betweenIndex, str.charAt(i) + "");
				else
					betweens.set(betweenIndex, betweens.get(betweenIndex) + str.charAt(i));
			}
		}
		
		String[] betweensArray = new String[betweens.size()];
		for(int i = 0; i < betweensArray.length; i++)
			betweensArray[i] = betweens.get(i);
		return betweensArray;
	}
	
	public static String removeTrailingWhiteSpace(String s)
	{
		//html lang="en" dir="ltr"
		
		if(s == null)
			return null;
		
		if(s.length() == 0)
			return s;
		
		int startIndex = 0, stopIndex = 0;
		
		for(int i = 0; i < s.length(); i++)
		{
			if(s.charAt(i) != ' ' && s.charAt(i) != '\t')
			{
				startIndex = i;
				break;
			}
		}
		
		for(int i = s.length() - 1; i >= startIndex; i--)
		{
			if(s.charAt(i) != ' ' && s.charAt(i) != '\t')
			{
				stopIndex = i;
				break;
			}
		}
		
		return s.substring(startIndex, stopIndex + 1);
	}
	
	public String[] getStringsBetween(String str, String between)
	{
		return getStringsBetween(str, between, between);
	}
	
	public static int iRandomRange(int min, int max)
	{
		return (int)Math.round(Math.random() * (max - min) + min);
	}
	
	public static boolean within(double x, double minX, double maxX)
	{
		return x >= minX && x <= maxX;
	}
	
	public static boolean within(double x, double y, double minX, double minY, double maxX, double maxY)
	{
		return within(x, minX, maxX) && within(y, minY, maxY);
	}
	
	public static int round(double d)
	{
		return (int)(d + 0.5);
	}
	
	public static int floor(double d)
	{
		return (int)Math.floor(d);
	}
	
	public static int ceil(double d)
	{
		return (int)Math.ceil(d);
	}

	public static String removeSpaces(String str)
	{
		String noSpaces = "";
		for(int i = 0; i < str.length(); i++)
			noSpaces += str.charAt(i) != ' ' ? str.charAt(i) : "";
		return noSpaces;
	}
}
