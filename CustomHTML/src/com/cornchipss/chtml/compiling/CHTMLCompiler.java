package com.cornchipss.chtml.compiling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cornchipss.chtml.CustomHTML;
import com.cornchipss.chtml.TagRegistry;
import com.cornchipss.chtml.results.Result;
import com.cornchipss.chtml.tags.ICustomTag;
import com.cornchipss.chtml.util.CachedFile;
import com.cornchipss.chtml.util.HTMLUtils;
import com.cornchipss.chtml.util.Helper;
import com.cornchipss.chtml.util.TagUtils;

public class CHTMLCompiler
{
	/**
	 * If this text is present at the first line of a file that is being compiled,
	 * the file will not be outputted into the compiled files directory
	 */
	public static final String NO_OUTPUT_FLAG = "<!--NOOUTPUT-->";
	
	/**
	 * Caches files that have already been compiled
	 */
	private static Map<File, List<CachedFile>> cachedFiles = new HashMap<>();
	
	private static String[] compilableExtensions;
	
	/**
	 * Parses the CustomHTML in a file into HTML
	 * @param f The file to parse
	 * @param variables Any variables that should be present
	 * @param stopIfNoOutput Stops the compillation if the NOOUTPUT flag is found and this is true, if this is false it ignores it
	 * @return The parsed CustomHTML as a String of lines consiting of normal HTML
	 * @throws IOException If there is an error reading from the file
	 */
	public static String compile(File f, Map<String, String> variables, boolean stopIfNoOutput) throws IOException
	{
		// Chaching saves time compiling many files if one file (for example a nav template) is the same for each file so it isn't recompiled every time
		if(cachedFiles.containsKey(f))
		{
			if(stopIfNoOutput)
			{
				BufferedReader br = new BufferedReader(new FileReader(f));
				String firstLine = br.readLine();
				br.close();
				
				if(firstLine != null && isNoOutput(firstLine))
					return null;
			}
			
			List<CachedFile> fileVariations = cachedFiles.get(f);
			
			for(CachedFile cached : fileVariations)
			{
				Set<String> cachedVars = cached.variables.keySet();
				Set<String> vars = variables.keySet();
				
				boolean isSame = true;
				
				if(vars.size() == cachedVars.size())
				{
					for(String s : vars)
					{
						if(!cachedVars.contains(s))
						{
							isSame = false;
							break;
						}
					}
				}
				else
					isSame = false;
				
				if(isSame)
					return cached.lines; // It's the same file and same variables, so no need to re-parse it
			}
		}
		
		// These cloned variables will be used to store in a cached file
		Map<String, String> variablesCloned = new HashMap<>();
		for(String s : variables.keySet())
			variablesCloned.put(s, variables.get(s));
		
		String lines = readCompilableFile(f, stopIfNoOutput);
		
		if(lines == null)
			return null;
		
		int tagStart = lines.indexOf("<");
		int tagEnd = lines.indexOf(">");

		mainLoop:
		while(tagStart != -1 && tagEnd != -1)
		{
			String tagString;

			try
			{
				tagString = TagUtils.stripTag(Helper.removeTrailingWhiteSpace(lines.substring(tagStart + 1, tagEnd)));
			}
			catch(Exception ex)
			{								
				StringBuilder toWrite = new StringBuilder();
				
				toWrite.append("!! BAD HTML TAG OPENING/CLOSING DETECTED IN FILE \"" + f.getPath() + "\" !!\n");
				toWrite.append("== Debug Info ==\n");
				toWrite.append("Tag Start: " + tagStart + " | Tag End: " + tagEnd + "\n");
				toWrite.append("Start text: " + lines.substring(tagStart) + "\n");
				toWrite.append("End text: " + lines.substring(tagEnd) + "\n");
				
				System.out.println(toWrite);
				
				toWrite.append("\nFile Parsed\n");
				toWrite.append(lines);
				
				BufferedWriter bw = new BufferedWriter(new FileWriter("error-log.txt"));
				bw.write(toWrite.toString());
				bw.close();
				throw ex;
			}
			
			if(TagUtils.isClosingTag(tagString))
				tagString = Helper.removeTrailingWhiteSpace(tagString.substring(1)); // remove the pesky '/'
			
			String name = TagUtils.getName(tagString).replace("\n", "").replace("\r", "");
			
			// Handles Any Tags That Their Insides Shouldn't Be Touched By Me

			boolean wasInIgnoredCode = false;
			
			for(int i = 0; i < CustomHTML.ignoredTagsWithChunks.length; i += 2)
			{
				if(name.equalsIgnoreCase(CustomHTML.ignoredTagsWithChunks[i]))
				{
					wasInIgnoredCode = true;
					int[] startEnd = HTMLUtils.findClosingTag(CustomHTML.ignoredTagsWithChunks[i + 1], lines, tagStart + 1);
					tagStart = startEnd[0];
					tagEnd = startEnd[1] + 1;
					tagStart++;
					
					continue mainLoop;
				}
			}
			
			if(tagStart > tagEnd)
			{
				tagStart = -1;
				tagEnd = -1;
			}

			if(wasInIgnoredCode)
				continue;
			
			if(Helper.indexOf(CustomHTML.skipTags, name.toLowerCase()) == -1)
			{
				/**
				 * Attributes are stored in here as attribute name: value.
				 * If the attribute has no value but is present, it is assigned to be an empty String
				 */
				Map<String, String> attributes = TagUtils.getAttributes(tagString, name);
				
				if(TagRegistry.getTag(name) != null)
				{
					ICustomTag customTag = TagRegistry.getTag(name);
					
					Map<String, String> theirVariables = new HashMap<>();
					theirVariables.putAll(variables);
					theirVariables.putAll(attributes);
					
					Result[] results = customTag.use(lines, attributes, tagStart, tagEnd, theirVariables, variables);

					if(!CustomHTML.isRunning())
					{
						System.err.println("Compilation stopped during this file's compilation: " + f.getAbsolutePath());
						System.exit(1);
					}

					if(results != null)
					{
						String firstHalf = lines.substring(0, tagStart);

						// Go through the results backwards so their indexes aren't messed up
						for(int i = results.length - 1; i >= 0; i--)
						{
							if(results[i] == null)
								continue; // There was no change
							
							int replaceStart = results[i].getStart();
							int replaceEnd = results[i].getEnd();

							firstHalf = lines.substring(0, replaceStart);
							String lastHalf = lines.substring(replaceEnd);

							lines = firstHalf + results[i].getRepWith() + lastHalf;
						}

						// Reset the tag end to the last bit added, and there's not need to reset the first as that's reset later
						int length;

						if(results[0] == null || results[0].getRepWith() == null)
						 	length = tagEnd;
						else
							length = results[0].getRepWith().length();

						tagEnd = firstHalf.length() + length - 1;
					}
				}
			}

			int[] nextTags = HTMLUtils.findNextTag(lines, tagEnd);
			tagStart = nextTags[0];
			tagEnd = nextTags[1];
		}
		
		List<CachedFile> here = cachedFiles.get(f);
		if(here == null)
			here = new ArrayList<>();
		
		here.add(new CachedFile(lines, variablesCloned));
		
		cachedFiles.put(f, here);
		
		return lines;
	}
	
	/**
	 * Returns true if this file should be parsed, false if not
	 * @param f The file to check if it should be parsed
	 * @return true if it should be parsed, false if not
	 */
	public static boolean isFileToParse(File f)
	{
		String extension = Helper.getExtension(f);
		for(String s : compilableExtensions)
			if(s.equals(extension))
				return true;
		return false;
	}
	
	/**
	 * Reads all the text in the file while removing any compiler flags
	 * @param f The file to read from
	 * @param stopIfNoOutput If this is a no output file, the reader will stop & return null
	 * @return The lines without any compiler flags OR null if the "stopIfNoOutput" variable is true & this file had that compiler directive
	 */
	public static String readCompilableFile(File f, boolean stopIfNoOutput)
	{
		StringBuilder builder = new StringBuilder();
		
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(f));
			boolean firstLine = true;
			for(String line = br.readLine(); line != null; line = br.readLine())
			{
				if(firstLine && isNoOutput(line))
				{
					if(stopIfNoOutput)
					{
						br.close();
						return null;
					}
					else
						line = Helper.removeTrailingWhiteSpace(line.substring(NO_OUTPUT_FLAG.length())); // Remove the NOOUTPUT comment at the top
				}
				
				firstLine = false;
				
				builder.append(line);
				builder.append(System.lineSeparator());
			}
			br.close();
		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
		
		return builder.toString();
	}
	
	/**
	 * Determines if a given file should be output to the output directory based on a compiler directive line
	 * @param line The line to check
	 * @return false if a given file should be output to the output directory based on a compiler directive line, otherwise true
	 */
	public static boolean isNoOutput(String line)
	{
		if(line.length() >= NO_OUTPUT_FLAG.length())
		{
			String substr = line.substring(0, NO_OUTPUT_FLAG.length());
			if(substr.equalsIgnoreCase(NO_OUTPUT_FLAG))
			{
				return true;
			}
		}
		return false;
	}

	public static void setCompilableExtensions(String[] extensions)
	{
		compilableExtensions = extensions;
	}
}
