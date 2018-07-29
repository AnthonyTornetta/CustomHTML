package com.cornchipss.chtml.core.tags;

import java.util.Map;

import com.cornchipss.chtml.results.Result;
import com.cornchipss.chtml.tags.ICustomTag;

public class VarTag implements ICustomTag
{
	@Override
	public Result[] use(String lines, Map<String, String> attributes, int tagStart, int tagEnd, Map<String, String> localVars, Map<String, String> outerVars)
	{
		String varName = attributes.get("name");
		if(varName != null) // var is also a valid HTML tag so don't worry about it
		{
			String value = attributes.get("value");			
			if(value == null)
			{
				return new Result[] { new Result(localVars.get(varName), tagStart, tagEnd + 1) };
			}
			else
			{
				// Setting the outerVars so the value is added to the global variable, as opposed to this tag's local variables
				outerVars.put(varName, value);
				return new Result[] { new Result("", tagStart, tagEnd + 1) };
			}
		}
		
		return null;
	}

	@Override
	public String getName() { return "var"; }

	@Override
	public boolean hasPartner() { return false; }
}
