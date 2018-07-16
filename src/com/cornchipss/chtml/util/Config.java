package com.cornchipss.chtml.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Config 
{
	public static final int BAD_VALUE = -9999;
	
	private File configFile;
	
	ArrayList<String> lines = new ArrayList<>();
	
	/**
	 * Reads and rights to a config file using the following format:
	 * <br>
	 * [key]: [data]
	 * <br>
	 * The config file will be created if it doesn't exist
	 * <br>
	 * The file saving format is of the .yml file type, thus files should be saved with a .yml extension, but you can make the extension anything you want
	 * @param cfgFile The file to read/write from
	 */
	public Config(String path)
	{
		try
		{
			configFile = new File(path);
			if(!configFile.exists())
			{
				configFile.createNewFile();
			}
		    InputStream fis = new FileInputStream(configFile);
		    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		    BufferedReader br = new BufferedReader(isr);
		    
		    // Read all the lines and put them into the array list
		    for(String line = br.readLine(); line != null; line = br.readLine())
		    {
		        lines.add(line);
		    }
		    br.close();
		} 
		catch (FileNotFoundException ex) 
		{
			ex.printStackTrace();
		} 
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * Sets a integer value at a given key
	 * @param key The key to set the value at
	 * @param integer The value the integer will have
	 */
	public void setInt(String key, int integer)
	{
		String newValue = key + ": " + integer;
		for(int i = 0; i < lines.size(); i++)
		{
			String[] split = lines.get(i).split(": ");
			if(split[0].equalsIgnoreCase(key))
			{
				lines.set(i, newValue);
				return;
			}
		}
		lines.add(newValue);
	}
	
	/**
	 * Sets a double value at a given key
	 * @param key The key to set the value at
	 * @param d The value the double will have
	 */
	public void setDouble(String key, double d)
	{
		String newValue = key + ": " + d;
		for(int i = 0; i < lines.size(); i++)
		{
			String[] split = lines.get(i).split(": ");
			if(split[0].equalsIgnoreCase(key))
			{
				lines.set(i, newValue);
				return;
			}
		}
		lines.add(newValue);
	}
	
	/**
	 * Sets a String value at a given key
	 * @param key The key to set the value at
	 * @param str The value the String will have
	 */
	public void setString(String key, String str)
	{
		String newValue;
		if(str == null)
			newValue = key + ": null";
		else
			newValue = key + ": " + str;
		
		for(int i = 0; i < lines.size(); i++)
		{
			String[] split = lines.get(i).split(": ");
			if(split[0].equalsIgnoreCase(key))
			{
				lines.set(i, newValue);
				return;
			}
		}
		lines.add(newValue);
	}
	
	/**
	 * Checks if the configuration file contains a key
	 * @param key The key to check if it exists
	 * @return True if it exists; False if it doesn't
	 */
	public boolean containsKey(String key)
	{
		for(int i = 0; i < lines.size(); i++)
		{
			String[] split = lines.get(i).split(": ");
			if(split[0].equalsIgnoreCase(key))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets a String value from the config file<br>Returns BAD_VALUE if the key isn't found
	 * @param key The key the value is stored at
	 * @return The String that was found or BAD_VALUE if it wasn't found
	 */
	public String getString(String key)
	{
		for(int i = 0; i < lines.size(); i++)
		{
			String[] split = lines.get(i).split(": ");
			if(split[0].equalsIgnoreCase(key))
			{
				if(split.length > 1)
				{
					String strToReturn = "";
					for(int j = 1; j < split.length; j++)
					{
						strToReturn += split[j];
					}
					return strToReturn;
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets a integer value from the config file<br>Returns BAD_VALUE if the key isn't found
	 * @param key The key the value is stored at
	 * @return The integer that was found or BAD_VALUE if it wasn't found
	 */
	public int getInt(String key)
	{
		for(int i = 0; i < lines.size(); i++)
		{
			String[] split = lines.get(i).split(": ");
			if(split[0].equalsIgnoreCase(key))
			{
				if(split.length > 1)
				{
					String possibleInt = split[1];
					if(Helper.isInt(possibleInt))
					{
						return Integer.parseInt(possibleInt);
					}
				}
			}
		}
		return BAD_VALUE;
	}
	
	/**
	 * Gets a double value from the config file<br>Returns BAD_VALUE if the key isn't found
	 * @param key The key the value is stored at
	 * @return The double that was found or BAD_VALUE if it wasn't found
	 */
	public double getDouble(String key)
	{
		for(int i = 0; i < lines.size(); i++)
		{
			String[] split = lines.get(i).split(": ");
			if(split[0].equalsIgnoreCase(key))
			{
				if(split.length > 1)
				{
					String possibleInt = split[1];
					if(Helper.isDouble(possibleInt))
					{
						return Double.parseDouble(possibleInt);
					}
				}
			}
		}
		return BAD_VALUE;
	}
	
	/**
	 * Saves the configuration file
	 * <br>
	 * Note: The config file can be saved multiple times, and there is no need to close it, just call this method to save it
	 * @throws IOException If there was some sort of error saving the file
	 */
	public void save() throws IOException
	{
		configFile.createNewFile();
		PrintWriter pw = new PrintWriter(configFile);
				
		pw.write("");
		for(int i = 0; i < lines.size(); i++)
		{
			String curLine = lines.get(i);
			System.out.println(curLine);
			pw.append(curLine);
			
			pw.append('\n');
		}
		pw.close();
	}
}
