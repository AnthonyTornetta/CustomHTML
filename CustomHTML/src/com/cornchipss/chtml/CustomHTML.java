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

import javax.swing.JOptionPane;

import com.cornchipss.chtml.bexp.OutsidePattern;
import com.cornchipss.chtml.exceptions.InvalidPluginYMLException;
import com.cornchipss.chtml.plugins.CustomHTMLPlugin;
import com.cornchipss.chtml.plugins.PluginLoader;
import com.cornchipss.chtml.results.Result;
import com.cornchipss.chtml.tags.ICustomTag;
import com.cornchipss.chtml.util.Config;
import com.cornchipss.chtml.util.Helper;

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
	 * A boolean flag that if false will stop the program's execution
	 */
	private static boolean running = true;
	
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

		int filesOutputed = 0;

		// Goes through each file and parses if it needed, otherwise copies it over
		for(int i = 0; i < files.size(); i++)
		{
			File f = files.get(i);

			String newLines = null;

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

				if(newLines == null)
				{
					System.out.println((i + 1) + "/" + files.size() + " files completed.");
					continue; // This is a no-output file
				}

				saveToCompiledDir(f, newLines);
			}
			else
			{
				// Copies the file to the new directory if they cannot be parsed
				copyFileToNewDir(f);
			}

			System.out.println((i + 1) + "/" + files.size() + " files completed.");
			filesOutputed++;
		}

		System.out.println("Complete! " + filesOutputed + " file" + (filesOutputed != 1 ? "s" : "") + " outputted!");
	}

	/**
	 * Loads the data from the config and creates it if it doens't exist
	 * @param cfg The config to read from
	 * @param args The arguments passed in from the user
	 * @throws IOException If there is an error saving the config
	 */
	private void loadConfig(Config cfg, String[] args) throws IOException
	{
		if(args.length == 2)
		{
			cfg.setString("relative-dir", args[0]);
			cfg.setString("save-to", args[1]);
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

		String extensions = cfg.getString("compilable-extensions");

		compilableExtensions = extensions.replace(" ", "").split(",");
		relativeDir = cfg.getString("relative-dir");
		saveTo = cfg.getString("save-to");
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

	// TODO: Make replaceAllNeeded less sloppy

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
				System.out.println("Start/End: " + tagStart + " | finish: " + tagEnd);
				System.out.println("Start text: " + lines.substring(tagStart));
				System.out.println("End text: " + lines.substring(tagEnd));
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

			// Handles Any Tags That Their Insides Shouldn't Be Touched By Me

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
				int[] startEnd = findClosingTag("?>", lines, tagEnd + 1);
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
				 * Attributes are stored in here as attribute name: value.
				 * If the attribute has no value but is present, it is assigned to be an empty String
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

							if(splitEquals.length > 1) // for full attributes (something="asdf")
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
							else if(splitEquals.length == 1) // There are some attributes w/ only a title and no value (e.g. download in the <a> tag), so handle those differently
							{
								attributes.put(splitEquals[0], ""); // Give it the value of an empty String
							}
							else
							{
								continue; // A line that is being skipped has been hit and has no characters after it

								/*
								 * This has been shown to happen during multiline comments that begin like this:
								 * <!--
								 * ...
								 * -->
								 * With no whitespace after the comment opening
								 */
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

						// A tag was found, no need to keep looking
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
