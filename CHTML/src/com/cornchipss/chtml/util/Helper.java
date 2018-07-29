package com.cornchipss.chtml.util;

import java.io.File;
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
		try
		{
			Integer.parseInt(possibleNumber);
			return true;
		}
		catch(NumberFormatException ex)
		{
			return false;
		}
	}
	
	public static boolean isDouble(String possibleDouble)
	{
		try
		{
			Double.parseDouble(possibleDouble);
			return true;
		}
		catch(NumberFormatException ex)
		{
			return false;
		}
	}
	
	/**
	 * Recursively adds files to a given list. This trickles down directories so no need to search them specifically
	 * @param folder The folder to start the search in
	 * @param fileList The list to add the files to
	 */
	public static void addFiles(File folder, List<File> fileList)
	{
		if(!folder.isDirectory())
			throw new IllegalStateException("Folder must be directory!");
		
		List<File> directories = new ArrayList<>(); // So directories are added afterwards
		
		for(File file : folder.listFiles())
		{
			if(file.isDirectory())
			{
				directories.add(file);
			}
			else
			{
				fileList.add(file);
			}
		}
		
		// This way it lists the files in the directory it's currently in first before delving deeper too avoid a disorganized mess
		for(File file : directories)
		{
			addFiles(file, fileList);
		}
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
	
	public static String getExtension(File f)
	{
		if(f.getName().contains("."))
			return f.getName().substring(f.getName().lastIndexOf(".") + 1);
		else
			return "";
	}
}
