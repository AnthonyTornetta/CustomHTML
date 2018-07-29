package com.cornchipss.chtml.plugins;

import com.cornchipss.chtml.util.Reference;

public abstract class CustomHTMLPlugin
{
	private String version;
	private String author;
	private String name;
	
	/**
	 * Run when the plugin is enabled.
	 * Usually used to add all the Tags
	 */
	public abstract void enable();
	
	/**
	 * Returns the CustomHTML version this plugin was built on
	 * @return The CustomHTML version this plugin was built on
	 */
	public final String getCustomHTMLVersion() { return Reference.CHTML_VERSION; }
	
	
	// Getters & Setters //
	
	public String getVersion() { return version; }
	public void setVersion(String version) { this.version = version; }

	public String getAuthor() { return author; }
	public void setAuthor(String author) { this.author = author; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
}
