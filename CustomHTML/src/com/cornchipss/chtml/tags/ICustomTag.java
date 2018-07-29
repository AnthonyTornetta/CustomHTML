package com.cornchipss.chtml.tags;

import java.util.Map;

import com.cornchipss.chtml.results.Result;

public interface ICustomTag
{
	/**
	 * Uses the tag at the given spot and converts it to normal HTML or other custom tags
	 * @param lines Every line in the file
	 * @param attributes The attributes given to the tag in this instance
	 * @param tagStart The tag's beginning index in the lines
	 * @param tagEnd The tag's ending index in the lines
	 * @param localVars All variables present in the calling function and passed in via the vars attribute. Modifications to this will NOT affect the varibles for the rest of the file
	 * @param outerVars All variables present in the calling function but not those passed in via the vars attribute. Modifications to this WILL affect the varibles for the rest of the file
	 * @return The lines to change the tag to between the start and end of the tag -- DO NOT RETURN EVERY LINE, if there are no changest simply return the lines.substring(tagStart, tagEnd + 1) to return the originial tag.
	 */
	public abstract Result[] use(String lines, Map<String, String> attributes, int tagStart, int tagEnd, Map<String, String> localVars, Map<String, String> outerVars);
	
	/**
	 * Returns the name of the tag
	 * @return The name of the tag
	 */
	public abstract String getName();

	/**
	 * Does the tag have a partner opening/closing tag?
	 * @return True if it should have a partner opening/closing tag, false if not.
	 */
	public abstract boolean hasPartner();
}
