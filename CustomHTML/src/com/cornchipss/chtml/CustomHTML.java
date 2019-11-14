package com.cornchipss.chtml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JOptionPane;

import com.cornchipss.chtml.compiling.CHTMLCompiler;
import com.cornchipss.chtml.exceptions.InvalidPluginYMLException;
import com.cornchipss.chtml.plugins.PluginLoader;
import com.cornchipss.chtml.util.Config;
import com.cornchipss.chtml.util.DirectoryUtils;
import com.cornchipss.chtml.util.Helper;

public final class CustomHTML
{
	/**
	 * The directory to get the files to compile in
	 */
	private static String relativeDir;
	
	/**
	 * All files that should not be copied to new directory
	 */
	private static String[] ignoredFiles;
	
	/**
	 * A boolean flag that if false will stop the program's execution
	 */
	private static boolean running = true;
	
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
	
	public static final String PLUGINS_DIR = "plugins/";
	
	private static final Map<String, String> globalVariables = new HashMap<>();
	
	public static void main(String[] args) throws IOException, InvalidPluginYMLException
	{
		new CustomHTML(args);
	}
	
	private CustomHTML(String[] args) throws IOException, InvalidPluginYMLException
	{
		System.out.println("Starting...");

		Config cfg = new Config("processor-config.yml");
		
		loadConfig(cfg, args);

		File pluginsDirectory = new File(PLUGINS_DIR);
		pluginsDirectory.mkdir(); // Make sure the plugins directory exists before trying to use it
		
		loadGlobalVars();
		
		PluginLoader.loadPlugins(pluginsDirectory);

		TagRegistry.init();
		
		// Every file in the relativeDir directory
		List<File> files = new ArrayList<>();
		
		Helper.addFiles(new File(relativeDir), files);

		System.out.println(files.size() + " files found to process...");

		long millis = System.currentTimeMillis();
		
		int filesOutputted = processFiles(files);
		
		if(filesOutputted != -1)
		{
			System.out.println("Complete! " + filesOutputted + " file" + (filesOutputted != 1 ? "s" : "") + " outputted!");
			System.out.println("Took " + (System.currentTimeMillis() - millis) + "ms to complete!");
		}
	}

	private int processFiles(List<File> files)
	{
		int filesOutputted = 0;
		
		// Goes through each file and parses if it needed, otherwise copies it over
		for(int i = 0; i < files.size(); i++)
		{
			File f = files.get(i);

			String newLines = null;
			
			String extension = f.getName().substring(f.getName().lastIndexOf(".") + 1).toLowerCase();
			
			if(CHTMLCompiler.isFileToParse(f))
			{
				try
				{
					newLines = CHTMLCompiler.compile(f, new HashMap<>(getGlobalVars()), true);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					System.out.println("File " + f.getPath() + " had an unkown error during parsing ;(");
					return -1;
				}

				if(newLines != null)
				{
					try
					{
						DirectoryUtils.saveToCompiledDir(f, newLines);
						filesOutputted++;
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
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
					try
					{
						DirectoryUtils.copyFileToNewDir(f);
						filesOutputted++;
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
			}

			System.out.println((i + 1) + "/" + files.size() + " files completed.");
		}
		
		return filesOutputted;
	}

	private void loadGlobalVars()
	{
		try
		{
			Config vars = new Config(PLUGINS_DIR + "vars.yml");
			
			for(String s : vars.getStrings())
			{
				globalVariables.put(s, vars.getString(s));
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
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
		
		ignoredFiles = cfg.getString("no-copy-extensions").replace(" ", "").split(",");
		relativeDir = cfg.getString("relative-dir");
		
		DirectoryUtils.setSaveTo(cfg.getString("save-to"));
		DirectoryUtils.setRelativeDir(relativeDir);
		
		CHTMLCompiler.setCompilableExtensions(cfg.getString("compilable-extensions").replace(" ", "").split(","));
	}
	
	/**
	 * Stops the compilation
	 * @param error The error to display
	 */
	public static void stopRunning(String error)
	{
		System.err.println(error);
		System.out.println("STOPPING COMPILATION!");

		running = false;
	}
	
	public static Map<String, String> getGlobalVars() { return globalVariables; }

	public static boolean isRunning() { return running; }
}
