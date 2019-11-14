package com.cornchipss.chtml.core.tags;

import java.util.Map;

import com.cornchipss.chtml.results.Result;
import com.cornchipss.chtml.tags.ICustomTag;
import com.cornchipss.chtml.util.HTMLUtils;
import com.cornchipss.chtml.util.Helper;

public class IfTag implements ICustomTag
{
	@Override
	public Result[] use(String lines, Map<String, String> attributes, int tagStart, int tagEnd, Map<String, String> localVars, Map<String, String> outerVars)
	{
		String x = attributes.get("x");
		
		String y = attributes.get("y");
		
		boolean evaluation = false;
		
		if(x != null && y != null)
		{
			if(x.length() > 0)
			{
				if(x.charAt(0) == '$')
					x = localVars.get(x.substring(1));
				if(y.charAt(0) == '$')
					y = localVars.get(y.substring(1));
				
				if(x == null)
					x = "undefined";
				if(y == null)
					y = "undefined";
			}
			
			if(attributes.get("greaterOrEqual") != null)
			{
				if(Helper.isDouble(x) && Helper.isDouble(y))
				{
					evaluation = Double.parseDouble(x) >= Double.parseDouble(y);
				}
				else
				{
					evaluation = x.compareTo(y) >= 0;
				}
			}
			else if(attributes.get("greater") != null)
			{
				if(Helper.isDouble(x) && Helper.isDouble(y))
				{
					evaluation = Double.parseDouble(x) > Double.parseDouble(y);
				}
				else
				{
					evaluation = x.compareTo(y) > 0;
				}
			}
			else if(attributes.get("less") != null)
			{
				if(Helper.isDouble(x) && Helper.isDouble(y))
				{
					evaluation = Double.parseDouble(x) < Double.parseDouble(y);
				}
				else
				{
					evaluation = x.compareTo(y) < 0;
				}
			}
			else if(attributes.get("lessOrEqual") != null)
			{
				if(Helper.isDouble(x) && Helper.isDouble(y))
				{
					evaluation = Double.parseDouble(x) <= Double.parseDouble(y);
				}
				else
				{
					evaluation = x.compareTo(y) <= 0;
				}
			}
			else if(attributes.get("equal") != null)
			{
				evaluation = x.equals(y);
			}
			else if(attributes.get("notEqual") != null)
			{
				evaluation = !x.equals(y);
			}
			else
			{
				evaluation = false;
			}
		}
		
		int[] closingTagPos = HTMLUtils.findClosingTag("</" + getName() + ">", lines, tagEnd + 1);
		int closingStart = closingTagPos[0];
		int closingEnd = closingTagPos[1];
		
		if(!evaluation)
		{
			return new Result[] {new Result("", tagStart, closingEnd + 1)};
		}
		else
		{
			return new Result[] {new Result("", tagStart, tagEnd + 1), new Result("", closingStart, closingEnd + 1)};
		}
	}

	@Override
	public String getName() { return "if"; }
	@Override
	public boolean hasPartner() { return true; }
}
