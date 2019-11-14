package com.cornchipss.chtml.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cornchipss.chtml.CustomHTML;
import com.cornchipss.chtml.exceptions.InvalidPluginYMLException;
import com.cornchipss.chtml.util.Config;
import com.cornchipss.chtml.util.Helper;

public final class PluginLoader
{
	/**
	 * Loads the plugin from the jar file and returns the CustomHTMLPlugin object made from the jar file
	 * @param f The jar file to load the plugin from. The jar file must have a plugin.yml file in its root directory.
	 * @return The CustomHTMLPlugin object made from the jar file
	 * @throws InvalidPluginYMLException If there is no YML file present or one of the required values isn't present
	 * @throws IOException If there is an error reading the jar file
	 */
	@SuppressWarnings("unchecked")
	public static CustomHTMLPlugin loadPlugin(File f) throws InvalidPluginYMLException, IOException
	{
		JarFile file = new JarFile(f);
		JarEntry ymlFile = file.getJarEntry("plugin.yml");
		
		if(ymlFile == null)
		{
			file.close();
			throw new InvalidPluginYMLException("YML File not found for plugin " + file.getName());
		}
		
		Config cfg = new Config(file.getInputStream(ymlFile));
		file.close();
		
		if(!cfg.containsKey("main"))
			throw new InvalidPluginYMLException("No value for the main class found!");
		
		URLClassLoader child = new URLClassLoader(new URL[] {
			new URL("file:///" + f.getAbsolutePath())
		}, CustomHTML.class.getClassLoader());
		
		Class<? extends CustomHTMLPlugin> clazz = null;
		
		try
		{
			clazz = (Class<? extends CustomHTMLPlugin>) Class.forName(cfg.getString("main"), true, child);
		}
		catch(ClassNotFoundException ex)
		{
			Logger.getLogger("err").log(Level.SEVERE, "Main class " + cfg.getString("main") + " not found!", ex);
			System.exit(1);
		}
		
		try
		{
			CustomHTMLPlugin plugin = clazz.newInstance();
			
			plugin.setName(cfg.getString("name"));
			plugin.setAuthor(cfg.getString("author"));
			plugin.setVersion(cfg.getString("version"));
			return plugin;
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		return null;
	}
	
	/**
	 * Loads all the plugins in the plugins folder
	 * @param pluginsDirectory The plugins directory to load them from
	 * @throws InvalidPluginYMLException If one of the plugins has an invalid YML
	 * @throws IOException If there was an error reading one of the plugin files
	 */
	public static void loadPlugins(File pluginsDirectory) throws InvalidPluginYMLException, IOException
	{
		System.out.println(pluginsDirectory.getAbsolutePath());
		for(File f : pluginsDirectory.listFiles())
		{
			System.out.println(f);
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
}
