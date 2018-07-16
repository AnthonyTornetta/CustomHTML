package com.cornchipss.chtml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.cornchipss.chtml.bexp.OutsidePattern;
import com.cornchipss.chtml.results.ReplaceResult;
import com.cornchipss.chtml.tags.ICustomTag;
import com.cornchipss.chtml.util.Config;
import com.cornchipss.chtml.util.Helper;

public class CustomHTML
{
	public static final String NO_OUTPUT_FLAG = "<!--NOOUTPUT-->";
	
	private static Config cfg;
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("Starting...");
		
		TagRegistry.reigsterAll();
		
		cfg = new Config("processor-config.yml");
		
		if(args.length == 3)
		{
			cfg.setString("relative-dir", args[0]);
			cfg.setString("save-to", args[1]);
			cfg.setString("search-in", args[2]);
			cfg.save();
		}
		
		if(!cfg.containsKey("relative-dir") || !cfg.containsKey("save-to") || !cfg.containsKey("search-in"))
		{			
			Scanner scan = new Scanner(System.in);
			System.out.print("Relative Directory (starting project directory): ");
			cfg.setString("relative-dir", scan.nextLine());
			System.out.print("Save compiled files to directory               : ");
			cfg.setString("save-to", scan.nextLine());
			System.out.print("Search for files to compile directory          : ");
			cfg.setString("search-in", scan.nextLine());
			
			cfg.save();
			scan.close();
		}
		
		List<String> ignoredFileTypes = new ArrayList<>();
		
		if(cfg.containsKey("ignored-file-types"))
		{
			String[] split = cfg.getString("ignored-file-types").split(",");
			ignoredFileTypes.addAll(Arrays.asList(split));
		}
		
		String searchDir = cfg.getString("search-in");
		
		List<File> files = new ArrayList<>();
		
		addFiles(new File(searchDir), files);
		
		System.out.println(files.size() + " files found to process...");
		
		int filesOutputed = 0;
		
		for(int i = 0; i < files.size(); i++)
		{
			File f = files.get(i);
			
			String newLines;
			
			if(isFileToParse(f))
			{
				try
				{
					newLines = replaceAllNeeded(f, new HashMap<>(), true);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					System.out.println("File " + f.getPath() + " had an error during parsing ;(");
					return;
				}
			}
			else
			{
				newLines = "";
				BufferedReader br = new BufferedReader(new FileReader(f));
				for(String line = br.readLine(); line != null; line = br.readLine())
					newLines += line + "\n";
				br.close();
			}
			
			if(newLines == null)
			{
				System.out.println((i + 1) + "/" + files.size() + " files completed.");
				continue; // This is a no-output file
			}
			
			String saveTo = cfg.getString("save-to");
			String slashOrNoSlash = saveTo.charAt(saveTo.length() - 1) == '/' ? "" : "/";
			
			String relDir = cfg.getString("relative-dir");
			
			String filesPath = f.getPath();
			int x = 0;
			while(x < filesPath.length() && relDir.indexOf(filesPath.substring(0, x + 1)) == 0)
				x++;
			
			String howToSave = filesPath.substring(x);
			
			String saveToPath = saveTo + slashOrNoSlash + howToSave;
			String directoriesPath = saveToPath.substring(0, saveToPath.lastIndexOf("\\") + 1);
			
			File pathTo = new File(directoriesPath);
			pathTo.mkdirs(); // Creates the file's directory if it doesn't exist	
			
			if(isFileToParse(f))
			{
				File newFile = new File(saveToPath);
				newFile.createNewFile();
			
				BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
				bw.write(newLines);
				bw.close();
			}
			else
			{
				String extension = f.getName().substring(f.getName().lastIndexOf(".") + 1);
				
				boolean badBoi = false;
				
				for(String type : ignoredFileTypes)
				{
					if(type.equals(extension))
					{
						badBoi = true;
						break;
					}
				}
				
				if(!badBoi)
					Files.copy(f.toPath(), FileSystems.getDefault().getPath(saveToPath), StandardCopyOption.REPLACE_EXISTING);
			}
			
			System.out.println((i + 1) + "/" + files.size() + " files completed.");
			filesOutputed++;
		}
		
		System.out.println("Complete! " + filesOutputed + " file" + (filesOutputed != 1 ? "s" : "") + " outputted!");
	}
	
	/**
	 * Parses the CustomHTML in a file into HTML
	 * @param f The file to parse
	 * @param variables Any variables that should be present
	 * @param stopIfNoOutput Stops the compillation if the NOOUTPUT flag is found and this is true, if this is false it ignores it
	 * @return The parsed CustomHTML as a String of lines consiting of normal HTML
	 * @throws IOException If there is an error reading from the file
	 */
	public static String replaceAllNeeded(File f, Map<String, String> variables, boolean stopIfNoOutput) throws IOException
	{
		String lines = "";
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		int lineNumber = 1;
		
		for(String line = br.readLine(); line != null; line = br.readLine())
		{
			if(line.length() >= NO_OUTPUT_FLAG.length())
			{
				String substr = line.substring(0, NO_OUTPUT_FLAG.length());
				if(substr.equalsIgnoreCase(NO_OUTPUT_FLAG) && lineNumber == 1)
				{
					if(stopIfNoOutput)
					{
						br.close();
						return null;
					}
					
					line = Helper.removeTrailingWhiteSpace(line.substring(NO_OUTPUT_FLAG.length()));
				}
				
				lineNumber++;
			}
			
			lines += line + "\n";
		}
		br.close();
		
		int tagStart = lines.indexOf("<");
		int tagEnd = lines.indexOf(">");
		
		while(tagStart != -1 && tagEnd != -1)
		{
			String tagString;
			
			try
			{
				tagString = Helper.removeTrailingWhiteSpace(lines.substring(tagStart + 1, tagEnd));
			}
			catch(Exception ex)
			{
				System.out.println("!! BAD HTML TAG OPENING/CLOSING DETECTED IN FILE \"" + f.getPath() + "\" !!");
				System.out.println("== Debug Info ==");
				System.out.println("start: " + tagStart + " | finish: " + tagEnd);
				System.out.println("start text: " + lines.substring(tagStart));
				System.out.println("end text: " + lines.substring(tagEnd));
				System.out.println("Stopping Compilation.");
				
				throw ex;
			}
			
			boolean isClosingTag = tagString.charAt(0) == '/';
			if(isClosingTag)
				tagString = Helper.removeTrailingWhiteSpace(tagString.substring(1)); // remove the pesky '/'
			
			// Finds the name of the tag by separating it from its attributes
			int space = tagString.indexOf(" ");
			if(space == -1)
				space = tagString.length();
			
			String name = tagString.substring(0, space);
			
			// Handles Any Tags That Their Insides Shouldn't Be Touched By Me //

			boolean wasInIgnoredCode = false;
			
			if(name.equals("!--")) // Ignores blocks of comments
			{
				wasInIgnoredCode = true;
				int[] startEnd = findClosingTag("-->", lines, tagStart + 1);
				tagStart = startEnd[0];
				tagEnd = startEnd[1] + 1;
				tagStart++;
			}
			else if(name.length() >= "?".length() && name.contains("?")) // Ignores blocks of php
			{
				wasInIgnoredCode = true;
				int[] startEnd = findClosingTag("<?", "?>", lines, tagEnd + 1);
				tagStart = startEnd[0];
				tagEnd = startEnd[1] + 1;
			}
			else if(name.equals("script")) // Ignores blocks of js
			{
				wasInIgnoredCode = true;
				int[] startEnd = findClosingTag("</script>", lines, tagEnd + 1);
				tagStart = startEnd[0];
				tagEnd = startEnd[1] + 1;
			}
			
			if(tagStart > tagEnd)
			{
				tagStart = -1;
				tagEnd = -1;
			}
			
			if(wasInIgnoredCode)
				continue;
			
			if(!name.equalsIgnoreCase("!DOCTYPE"))
			{
				/**
				 * Attributes are stored in here as attribute name, value.
				 * If the attribute has no value but is present, it is assigned to be ""
				 */
				Map<String, String> attributes = new HashMap<>();
				
				String attributesString = tagString.substring(name.length(), tagString.length());
				
				attributesString = Helper.removeTrailingWhiteSpace(attributesString);
				
				if(attributesString.length() != 0)
				{
					if(attributesString.charAt(attributesString.length() - 1) == '/')
					{
						attributesString = Helper.removeTrailingWhiteSpace(attributesString.substring(0, attributesString.length() - 1));
					}
					
					if(attributesString.length() != 0)
					{
						String[] attrsSplitSpace = OutsidePattern.split(attributesString, " ", "\"");
												
						for(String attrLine : attrsSplitSpace)
						{
							String[] splitEquals = OutsidePattern.split(attrLine, "=", "\"");
							
							if(splitEquals.length > 1) // There are some attributes w/ only a title and no value (e.g. download in the <a> tag)
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
							else
							{
								attributes.put(splitEquals[0], "");
							}
						}
					}
				}
				
				for(ICustomTag customTag : TagRegistry.getTags())
				{
					if(customTag.getName().equalsIgnoreCase(name))
					{
						Map<String, String> theirVariables = new HashMap<>();
						theirVariables.putAll(variables);
						
						String varsStr = attributes.get("vars");
						if(varsStr != null)
						{
							String[] vars = OutsidePattern.split(varsStr, "=", "'");
							for(int i = 0; i < vars.length; i += 2)
							{
								theirVariables.put(vars[0], vars[1].substring(1, vars[1].length() - 1)); // Removes the apostrophies surrounding the variable
							}
						}
						
						ReplaceResult[] results = customTag.use(lines, attributes, tagStart, tagEnd, theirVariables, variables);
						
						if(results != null)
						{
							String firstHalf = lines.substring(0, tagStart);
							
							for(int i = results.length - 1; i >= 0; i--)
							{
								if(results[i] == null)
									continue; // There was no change
								
								int rmS = results[i].getStart();
								int rmE = results[i].getEnd();
								
								firstHalf = lines.substring(0, rmS);
								String lastHalf = lines.substring(rmE);
								
								lines = firstHalf + results[i].getRepWith() + lastHalf;
							}
							
							// Reset the tag end to the last bit added, and there's not need to reset the first as that's reset later
							int len = results[0] != null ? results[0].getRepWith().length() : tagEnd;
							tagEnd = firstHalf.length() + len - 1;
							
						}
						else
						{
							System.out.println("NULL @ " + f.getAbsolutePath()); // for debugging
						}
						break;
					}
				}
			}
			
			if(tagEnd + 1 < lines.length() - 1)
			{
				String substring = lines.substring(tagEnd + 1);
				
				int chunkOfLineBefore = lines.indexOf(substring);
				
				int tempStart = substring.indexOf("<") + chunkOfLineBefore;
				int tempEnd = substring.indexOf(">") + chunkOfLineBefore;
				
				if(tempStart > tagStart && tempEnd > tagEnd)
				{
					tagStart = tempStart;
					tagEnd = tempEnd;
				}
				else
				{
					tagStart = -1;
					tagEnd = -1;
				}
			}
			else
			{
				tagStart = -1;
				tagEnd = -1;
			}
		}
		
		return lines;
	}
	
	/**
	 * Finds the closing tag and returns the beginning index of the closing tag at the returned array index of 0, and the end tag's index at the returned array's index of 1
	 * @param openingTag The opening tag of the tag you're searching for
	 * @param closingTag The closing tag to search for
	 * @param lines The lines to search for the closing tag in
	 * @param startLookingAt The index to start the search at (after the initial opening tag)
	 * @return The beginning index of the closing tag at the returned array index of 0, and the end tag's index at the returned array's index of 1
	 */
	public static int[] findClosingTag(String openingTag, String closingTag, String lines, int startLookingAt)
	{
		int start = -1;
		
		int enclosedIn = 0;
		
		for(int i = startLookingAt; i + closingTag.length() <= lines.length(); i++)
		{
			if(i + openingTag.length() <= lines.length())
			{
				if(lines.substring(i, i + openingTag.length()).equalsIgnoreCase(openingTag))
				{
					enclosedIn++;
				}
			}
			
			if(lines.substring(i, i + closingTag.length()).equalsIgnoreCase(closingTag))
			{
				if(enclosedIn == 0)
				{
					start = i;
					break;
				}
				else
					enclosedIn--;
			}
		}
		
		if(start == -1)
			return new int[] { -1, -1 };
		else
			return new int[] { start, start + closingTag.length() - 1 };
	}
	
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
			return new int[] { start, start + closingTag.length() - 1 };
	}
	
	/**
	 * Finds the absolute path of a directory that is relative<br>
	 * For example: "/index.html" if the absolute path was "C:/asdf/stuff/" would return "C:/asdf/stuff/index.html"
	 * @param dir The directory to get the absolute directory of
	 * @return The absolute path of a directory that is relative
	 */
	public static String calculateRelativeDir(String dir)
	{
		String relDir = cfg.getString("relative-dir");
		
		if(dir.charAt(0) == '/')
		{
			if(relDir.charAt(relDir.length() - 1) == '/')
				relDir = relDir.substring(0, relDir.length() - 1);
			
			dir = relDir + dir;
		}
		else
		{
			if(relDir.charAt(relDir.length() - 1) != '/')
				relDir += "/";
			dir = relDir + dir;
		}
		
		return dir;
	}
	
	/**
	 * Returns true if this file should be parsed, false if not
	 * @param f The file to check if it should be parsed
	 * @return true if it should be parsed, false if not
	 */
	private static boolean isFileToParse(File f)
	{
		String extension = f.getName().substring(f.getName().lastIndexOf(".") + 1).toLowerCase();
		return extension.equals("html") || extension.equals("php");
	}
	
	/**
	 * Recursively adds files to a given list. This trickles down directories so no need to search them specifically
	 * @param folder The folder to start the search in
	 * @param fileList The list to add the files to
	 */
	private static void addFiles(File folder, List<File> fileList)
	{
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
}
