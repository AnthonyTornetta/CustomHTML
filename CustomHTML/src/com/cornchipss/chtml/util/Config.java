package com.cornchipss.chtml.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Config
{
	/**
	 * The file to be saved and read from
	 */
	private File configFile = null;
	
	/**
	 * Keeps track of comments based of things respective keys so when the config is saved, the comments aren't lost
	 */
	private Map<String, String> comments = new HashMap<>();
	
	/**
	 * Stores every Integer in relation with their key
	 */
	private Map<String, Integer> ints = new HashMap<>();
	
	/**
	 * Stores every String in relation with their key
	 */
	private Map<String, String> strings = new HashMap<>();
	
	/**
	 * Stores every Double in relation with their key
	 */
	private Map<String, Double> doubles = new HashMap<>();
	
	/**
	 * Stores every Double array in relation with their key
	 */
	private Map<String, Double[]> doubleArrays = new HashMap<>();
	
	/**
	 * Stores every Integer array in relation with their key
	 */
	private Map<String, Integer[]> intArrays = new HashMap<>();
	
	public static final char COMMENT_CHAR = '#';
		
	/**
	 * <p>Reads and rights to a config file using the following format: <code>[key]: [data]</code></p>
	 * <p>Comments should be started on a seperate line from values, and the first character on that line must be a '#' character.</p>
	 * <p>The config file will be created if it doesn't exist.</p>
	 * <p>The file saving format is of the .yml file type.</p>
	 * @param path The path to the config file. If no file exists, it will be created when save() is called.
	 * @throws IOException If anything goes wrong
	 */
	public Config(String path) throws IOException
	{
		this(null, new File(path));
	}
	
	/**
	 * <p>Reads and rights to a config file using the following format: <code>[key]: [data]</code></p>
	 * <p>Comments should be started on a seperate line from values, and the first character on that line must be a '#' character.</p>
	 * <p>The config file will be created if it doesn't exist.</p>
	 * <p>The file saving format is of the .yml file type.</p>
	 * @param is The InputStream to the config file. If no file exists, it will be created when save() is called.
	 * @throws IOException If anything goes wrong
	 */
	public Config(InputStream is) throws IOException
	{
		this(new BufferedReader(new InputStreamReader(is, "UTF-8")), null);
	}
	
	private void makeFile() throws IOException
	{
		new File(configFile.getAbsolutePath().substring(0, configFile.getAbsolutePath().lastIndexOf("\\"))).mkdirs();
		configFile.createNewFile();
	}
	
	/**
	 * <p>Reads and rights to a config file using the following format: <code>[key]: [data]</code></p>
	 * <p>Comments should be started on a seperate line from values, and the first character on that line must be a '#' character.</p>
	 * <p>The config file will be created if it doesn't exist.</p>
	 * <p>The file saving format is of the .yml file type.</p>
	 * @param br The input of the config file. If this is null but the file is not, a BufferedReader will be created from that file.
	 * @param f The config file. If this is null, the config cannot be saved.
	 * @throws IOException If anything goes wrong
	 */
	private Config(BufferedReader br, File f) throws IOException
	{
		configFile = f;
		
		if(configFile != null)
		{
			makeFile();
			
			if(br == null)
				br = new BufferedReader(new FileReader(configFile));
		}
		// Preserves comments in the config file
	    StringBuilder commentStringBuilder = new StringBuilder();
		
	    // Read all the lines and put them into the array list
	    for(String line = br.readLine(); line != null; line = br.readLine())
	    {
        	if(line.length() >= 1)
        	{
        		if(Helper.removeTrailingWhiteSpace(line).charAt(0) != COMMENT_CHAR)
        		{
					String[] split = line.split(": ");
					if(split.length == 1)
						split = line.split(":"); // Check if they just had no space after the colon
					
					if(split.length > 1) // This is a thing we should parse
					{
						if(Helper.isInt(split[1]))
						{
							ints.put(split[0], Integer.parseInt(split[1]));
						}
						else if(Helper.isDouble(split[1]))
						{
							doubles.put(split[0], Double.parseDouble(split[1]));
						}
						else
						{
							boolean isIntArray = true;
							boolean isDoubleArray = true;
							
							// Check if it's a number array
							String[] superSplit = split[1].split(",");
							for(int i = 0; i < superSplit.length; i++)
							{
								superSplit[i] = Helper.removeTrailingWhiteSpace(superSplit[i]);
								if(!Helper.isInt(superSplit[i]))
									isIntArray = false;
								if(!isIntArray && !Helper.isDouble(superSplit[i]))
									isDoubleArray = false;
							}
							
							if(isIntArray)
							{
								Integer[] arr = new Integer[superSplit.length];
								for(int i = 0; i < superSplit.length; i++)
								{
									arr[i] = Integer.parseInt(superSplit[i]);
								}
								
								intArrays.put(split[0], arr);
							}
							else if(isDoubleArray)
							{
								Double[] arr = new Double[superSplit.length];
								for(int i = 0; i < superSplit.length; i++)
								{
									arr[i] = Double.parseDouble(superSplit[i]);
								}
								
								doubleArrays.put(split[0], arr);
							}
							else
								strings.put(split[0], split[1]); // If all else fails, it's a String
						}
						
						// Puts comments in by the key they were behind
						comments.put(split[0], commentStringBuilder.toString());
						commentStringBuilder.setLength(0); // Clears the string builder
					}
        		}
        		else
        		{
	        		commentStringBuilder.append(line + "\r\n");
        		}
        	}
	    }
	    
	    br.close();
	}

	/**
	 * Sets a integer value at a given key
	 * @param key The key to set the value at
	 * @param integer The value the key will store
	 */
	public void setInt(String key, int integer)
	{
		ints.put(key, integer);
	}
	
	/**
	 * Gets the Integer at the specified key
	 * @param key The key to get the Integer at
	 * @throws IllegalArgumentException if there is no key present
	 * @return The Integer at the specified key
	 */
	public int getInt(String key)
	{
		if(!ints.containsKey(key))
			throw new IllegalArgumentException("Key of " + key + " not found!");
		
		return ints.get(key);
	}
	
	/**
	 * Gets an Integer value from the config file if it exists, or sets the value if it doesn't
	 * @param key The key to access or set
	 * @param defaultVal The value to set the key to if the value is not present
	 * @return The value present if it exists, or defaultVal if it wasn't present
	 */
	public int getOrSetInt(String key, int defaultVal)
	{
		if(ints.containsKey(key))
			return getInt(key);
		
		setInt(key, defaultVal);
		return defaultVal;
	}
	
	/**
	 * Sets a double value at a given key
	 * @param key The key to set the value at
	 * @param d The value the double will have
	 */
	public void setDouble(String key, double d)
	{
		doubles.put(key, d);
	}
	
	/**
	 * Gets a double value from the config file<br>Returns BAD_VALUE if the key isn't found
	 * @param key The key the value is stored at
	 * @return The double that was found or BAD_VALUE if it wasn't found
	 */
	public double getDouble(String key)
	{
		if(!doubles.containsKey(key))
			throw new IllegalArgumentException("Key of " + key + " not found!");
		
		return doubles.get(key);
	}
	
	/**
	 * Gets a Double value from the config file if it exists, or sets the value if it doesn't
	 * @param key The key to access or set
	 * @param defaultVal The value to set the key to if the value is not present
	 * @return The value present if it exists, or defaultVal if it wasn't present
	 */
	public double getOrSetDouble(String key, double defaultVal)
	{
		if(doubles.containsKey(key))
			return getDouble(key);
		
		setDouble(key, defaultVal);
		return defaultVal;
	}
	
	/**
	 * Sets a String value at a given key
	 * @param key The key to set the value at
	 * @param str The value the String will have
	 */
	public void setString(String key, String str)
	{
		strings.put(key, str);
	}
	
	/**
	 * Gets a String value from the config file<br>Returns BAD_VALUE if the key isn't found
	 * @param key The key the value is stored at
	 * @return The String that was found or BAD_VALUE if it wasn't found
	 */
	public String getString(String key)
	{
		if(!strings.containsKey(key))
			throw new IllegalArgumentException("Key of " + key + " not found!");
		
		return strings.get(key);
	}
	
	/**
	 * Gets a String value from the config file if it exists, or sets the value if it doesn't
	 * @param key The key to access or set
	 * @param defaultVal The value to set the key to if the value is not present
	 * @return The value present if it exists, or defaultVal if it wasn't present
	 */
	public String getOrSetString(String key, String defaultVal)
	{
		if(strings.containsKey(key))
			return getString(key);
		
		setString(key, defaultVal);
		return defaultVal;
	}
	
	/**
	 * Sets a String value at a given key
	 * @param key The key to set the value at
	 * @param str The value the String will have
	 */
	public void setIntegerArray(String key, Integer[] arr)
	{
		intArrays.put(key, arr);
	}
	
	/**
	 * Gets a String value from the config file<br>Returns BAD_VALUE if the key isn't found
	 * @param key The key the value is stored at
	 * @return The String that was found or BAD_VALUE if it wasn't found
	 */
	public Integer[] getIntegerArray(String key)
	{
		if(!intArrays.containsKey(key))
			throw new IllegalArgumentException("Key of " + key + " not found!");
		
		return intArrays.get(key);
	}
	
	/**
	 * Gets an Integer Array from the config file if it exists, or sets the value if it doesn't
	 * @param key The key to access or set
	 * @param defaultVal The value to set the key to if the value is not present
	 * @return The value present if it exists, or defaultVal if it wasn't present
	 */
	public Integer[] getOrSetIntegerArray(String key, Integer[] defaultVal)
	{
		if(strings.containsKey(key))
			return getIntegerArray(key);
		
		setIntegerArray(key, defaultVal);
		return defaultVal;
	}
	
	/**
	 * Sets a String value at a given key
	 * @param key The key to set the value at
	 * @param str The value the String will have
	 */
	public void setDoubleArray(String key, Double[] arr)
	{
		doubleArrays.put(key, arr);
	}
	
	/**
	 * Gets a String value from the config file<br>Returns BAD_VALUE if the key isn't found
	 * @param key The key the value is stored at
	 * @return The String that was found or BAD_VALUE if it wasn't found
	 */
	public Double[] getDoubleArray(String key)
	{
		if(!doubleArrays.containsKey(key))
			throw new IllegalArgumentException("Key of " + key + " not found!");
		
		return doubleArrays.get(key);
	}
	
	/**
	 * Gets an Integer Array from the config file if it exists, or sets the value if it doesn't
	 * @param key The key to access or set
	 * @param defaultVal The value to set the key to if the value is not present
	 * @return The value present if it exists, or defaultVal if it wasn't present
	 */
	public Double[] getOrSetDoubleArray(String key, Double[] defaultVal)
	{
		if(doubles.containsKey(key))
			return getDoubleArray(key);
		
		setDoubleArray(key, defaultVal);
		return defaultVal;
	}
	
	/**
	 * Checks if the configuration file contains a key
	 * @param key The key to check if it exists
	 * @return True if it exists; False if it doesn't
	 */
	public boolean containsKey(String key)
	{
		return strings.containsKey(key) || doubles.containsKey(key) || ints.containsKey(key) || intArrays.containsKey(key) || doubleArrays.containsKey(key);
	}
	
	/**
	 * Saves the configuration file according to how yml files are formatted
	 * <br>
	 * Note: The config file can be saved multiple times, and there is no need to close it, just call this method to save it
	 * @throws IOException If there was some sort of error saving the file
	 */
	public void save() throws IOException
	{
		if(configFile == null)
		{
			throw new IllegalStateException("Config File without a file to save to cannot be saved!");
		}
		
		new File(configFile.getAbsolutePath().substring(0, configFile.getAbsolutePath().lastIndexOf("\\"))).mkdirs();
		configFile.createNewFile();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
		
		bw.write("");
		
		for(String key : ints.keySet())
		{
			if(comments.containsKey(key))
				bw.append(comments.get(key));
			bw.append(key + ": " + ints.get(key) + "\r\n");
		}
		
		for(String key : doubles.keySet())
		{
			if(comments.containsKey(key))
				bw.append(comments.get(key));
			bw.append(key + ": " + doubles.get(key) + "\r\n");
		}
		
		for(String key : strings.keySet())
		{
			if(comments.containsKey(key))
				bw.append(comments.get(key));
			bw.append(key + ": " + strings.get(key) + "\r\n");
		}
		
		for(String key : intArrays.keySet())
		{
			if(comments.containsKey(key))
				bw.append(comments.get(key));
			bw.append(key + ": ");
			for(int i = 0; i < intArrays.get(key).length; i++)
			{
				bw.append(intArrays.get(key)[i] + (i + 1 != intArrays.get(key).length ? ", " : ""));
			}
			bw.append("\r\n");
		}
		
		for(String key : doubleArrays.keySet())
		{
			if(comments.containsKey(key))
				bw.append(comments.get(key));
			bw.append(key + ": ");
			for(int i = 0; i < doubleArrays.get(key).length; i++)
			{
				bw.append(doubleArrays.get(key)[i] + (i + 1 != doubleArrays.get(key).length ? ", " : ""));
			}
			bw.append("\r\n");
		}
		
		bw.close();
	}
}
