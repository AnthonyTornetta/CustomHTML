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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JOptionPane;

import com.cornchipss.chtml.exceptions.InvalidPluginYMLException;
import com.cornchipss.chtml.plugins.CustomHTMLPlugin;
import com.cornchipss.chtml.plugins.PluginLoader;
import com.cornchipss.chtml.results.Result;
import com.cornchipss.chtml.tags.ICustomTag;
import com.cornchipss.chtml.util.CachedFile;
import com.cornchipss.chtml.util.Config;
import com.cornchipss.chtml.util.Helper;
import com.cornchipss.chtml.util.TagUtils;

public final class CustomHTML
{
	/**
	 * If this text is present at the first line of a file that is being compiled,
	 * the file will not be outputted into the compiled files directory
	 */
	public static final String NO_OUTPUT_FLAG = "<!--NOOUTPUT-->";
	
	/**
	 * If a file's extension is in this array, it will be compiled, otherwise it will just be copied over
	 */
	private static String[] compilableExtensions;
	
	/**
	 * The directory to get the files to compile in
	 */
	private static String relativeDir;
	
	/**
	 * The directory to save the compiled files to
	 */
	private static String saveTo;
	
	/**
	 * All files that should not be copied to new directory
	 */
	private static String[] ignoredFiles;
	
	/**
	 * A boolean flag that if false will stop the program's execution
	 */
	private static boolean running = true;
	
	/**
	 * Caches files that have already been compiled
	 */
	private static Map<File, List<CachedFile>> cachedFiles = new HashMap<>();
	
	public static final String[] ignoredTagsWithChunks = 
		{
			"!--", "-->",
			"?", "?>",
			"?php", "?>",
			"script", "</script>"
		};
	
	public static final String[] skipTags = 
		{
			"!doctype"	
		};
	
	public static void main(String[] args) throws IOException, InvalidPluginYMLException
	{
		new CustomHTML(args);
	}
	
	private CustomHTML(String[] args) throws IOException, InvalidPluginYMLException
	{
		System.out.println("Starting...");

		Config cfg = new Config("processor-config.yml");

		loadConfig(cfg, args);

		File pluginsDirectory = new File("plugins");
		pluginsDirectory.mkdir(); // Make sure the plugins directory exists before trying to use it

		loadPlugins(pluginsDirectory);

		TagRegistry.init();
		
		// Every file in the relativeDir directory
		List<File> files = new ArrayList<>();

		Helper.addFiles(new File(relativeDir), files);

		System.out.println(files.size() + " files found to process...");

		int filesOutputted = 0;

		long millis = System.currentTimeMillis();
		
		// Goes through each file and parses if it needed, otherwise copies it over
		for(int i = 0; i < files.size(); i++)
		{
			File f = files.get(i);

			String newLines = null;
			
			String extension = f.getName().substring(f.getName().lastIndexOf(".") + 1).toLowerCase();
			
			if(isFileToParse(f))
			{
				try
				{
					newLines = replaceAllNeeded(f, new HashMap<>(), true);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					System.out.println("File " + f.getPath() + " had an unkown error during parsing ;(");
					return;
				}

				if(newLines != null)
				{
					saveToCompiledDir(f, newLines);
					filesOutputted++;
				}
			}
			else
			{
				boolean copy = true;
				
				for(String ignoredExtension : ignoredFiles)
				{
					if(extension.equals(ignoredExtension))
					{
						copy = false;
						break;
					}
				}
				
				// Copies the file to the new directory if they cannot be parsed
				
				if(copy)
				{
					copyFileToNewDir(f);
					filesOutputted++;
				}
			}

			System.out.println((i + 1) + "/" + files.size() + " files completed.");
		}

		System.out.println("Complete! " + filesOutputted + " file" + (filesOutputted != 1 ? "s" : "") + " outputted!");
		System.out.println("Took " + (System.currentTimeMillis() - millis) + "ms");
	}

	/**
	 * Loads the data from the config and creates it if it doens't exist
	 * @param cfg The config to read from
	 * @param args The arguments passed in from the user
	 * @throws IOException If there is an error saving the config
	 */
	private void loadConfig(Config cfg, String[] args) throws IOException
	{	
		if(args.length >= 2)
		{
			cfg.setString("relative-dir", args[0]);
			cfg.setString("save-to", args[1]);
			
			if(args.length == 3)
				cfg.setString("no-copy-extensions", args[2]);
			
			cfg.save();
		}

		if(!cfg.containsKey("relative-dir") || !cfg.containsKey("save-to"))
		{
			try
			{
				Scanner scan = new Scanner(System.in);
				System.out.print("Relative Directory (starting project directory): ");
				cfg.setString("relative-dir", scan.nextLine());
				System.out.print("Save compiled files to directory               : ");
				cfg.setString("save-to", scan.nextLine());

				cfg.save();
				scan.close();
			}
			catch(Exception ex)
			{
				// They are not in a console so they cannot type in the directories.
				JOptionPane.showMessageDialog(null, "Please fill out the configuration file.", "Config File", JOptionPane.INFORMATION_MESSAGE);

				cfg.setComment("relative-dir", "The directory the files will be searched for.");
				cfg.setString("relative-dir", "");
				cfg.setComment("save-to", "The directory the files will be saved to.");
				cfg.setString("save-to", "");
				cfg.setComment("compilable-extensions", "What extensions will be compiled.");
				cfg.setString("compilable-extensions", "html, php");

				cfg.save();

				System.exit(0);
			}
		}

		if(!cfg.containsKey("compilable-extensions"))
		{
			cfg.setString("compilable-extensions", "html, php");
			cfg.save();
		}

		if(cfg.getString("relative-dir").isEmpty())
		{
			// This is only true if nothing was inputted into the console (which means this is not being run through the console)

			JOptionPane.showMessageDialog(null, "Please fill out the configuration file.", "Config File", JOptionPane.INFORMATION_MESSAGE);

			System.exit(1);
		}
		
		if(!cfg.containsKey("no-copy-extensions"))
		{
			cfg.setString("no-copy-extensions", "");
			cfg.save();
		}
		
		compilableExtensions = cfg.getString("compilable-extensions").replace(" ", "").split(",");
		relativeDir = cfg.getString("relative-dir");
		saveTo = cfg.getString("save-to");
		ignoredFiles = cfg.getString("no-copy-extensions").replace(" ", "").split(",");
	}

	/**
	 * Saves a new file to the new directory with the new lines
	 * @param f The old file to get the name of
	 * @param newLines The lines to put in the new file
	 * @throws IOException If there is some sort of saving error
	 */
	public static void saveToCompiledDir(File f, String newLines) throws IOException
	{
		File newFile = new File(getSaveToPath(f));
		newFile.createNewFile();

		BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
		bw.write(newLines);
		bw.close();
	}

	/**
	 * Copies the old file to the new file's location
	 * @param f The file to copy
	 * @throws IOException If there is an error finding the new path or copying
	 */
	public void copyFileToNewDir(File f) throws IOException
	{
		Files.copy(f.toPath(), FileSystems.getDefault().getPath(getSaveToPath(f)), StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Gets the full path to save the new version of the file to
	 * @param f The old file to make a new version of
	 * @return The path to the new file
	 */
	public static String getSaveToPath(File f)
	{
		// Checks if a slash should be added to the end of the directory
		String slashOrNoSlash = (saveTo.endsWith("/") || saveTo.endsWith("\\")) ? "" : "/";

		String filesPath = f.getPath();

		// Finds the index of where to place the new path
		int x = 0;
		while(x < filesPath.length() && relativeDir.indexOf(filesPath.substring(0, x + 1)) == 0)
			x++;

		String howToSave = filesPath.substring(x);

		String saveToPath = saveTo + slashOrNoSlash + howToSave;

		// Makes sure the directory actually exists
		String directoriesPath = saveToPath.substring(0, saveToPath.lastIndexOf("\\") + 1);

		File pathTo = new File(directoriesPath);
		pathTo.mkdirs(); // Creates the file's directory if it doesn't exist

		return saveToPath;
	}

	/**
	 * Loads all the plugins in the plugins folder
	 * @param pluginsDirectory The plugins directory to load them from
	 * @throws InvalidPluginYMLException If one of the plugins has an invalid YML
	 * @throws IOException If there was an error reading one of the plugin files
	 */
	private void loadPlugins(File pluginsDirectory) throws InvalidPluginYMLException, IOException
	{
		for(File f : pluginsDirectory.listFiles())
		{
			if(!f.isDirectory())
			{
				String extension = Helper.getExtension(f);
				if(extension.equalsIgnoreCase("jar"))
				{
					CustomHTMLPlugin p = PluginLoader.loadPlugin(f);
					
					System.out.println("Enabling plugin \"" + p.getName() + "\" V(" + p.getVersion() + ")" + " by " + p.getAuthor());
					p.enable();
				}
			}
		}
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
			
			System.out.println("A cached file was found!");
			
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
			
			for(int i = 0; i < ignoredTagsWithChunks.length; i += 2)
			{
				if(name.equalsIgnoreCase(ignoredTagsWithChunks[i]))
				{
					wasInIgnoredCode = true;
					int[] startEnd = findClosingTag(ignoredTagsWithChunks[i + 1], lines, tagStart + 1);
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
			
			if(Helper.indexOf(skipTags, name.toLowerCase()) == -1)
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

					if(!running)
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

			int[] nextTags = findNextTag(lines, tagEnd);
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
	
	/**
	 * Finds the absolute path of a directory that is relative<br>
	 * For example: "/index.html" if the absolute path was "C:/asdf/stuff/" would return "C:/asdf/stuff/index.html"
	 * @param dir The directory to get the absolute directory of
	 * @return The absolute path of a directory that is relative
	 */
	public static String calculateRelativeDir(String dir)
	{
		if(dir.charAt(0) == '/')
		{
			if(relativeDir.charAt(relativeDir.length() - 1) == '/')
				relativeDir = relativeDir.substring(0, relativeDir.length() - 1);

			dir = relativeDir + dir;
		}
		else
		{
			if(relativeDir.charAt(relativeDir.length() - 1) != '/')
				relativeDir += "/";
			dir = relativeDir + dir;
		}

		return dir;
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
	 * Stops the compilation
	 * @param error The error to display
	 */
	public static void stopCompilation(String error)
	{
		System.err.println(error);
		System.out.println("STOPPING COMPILATION!");

		running = false;
	}
}
