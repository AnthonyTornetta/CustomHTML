package com.cornchipss.chtml;

import java.util.ArrayList;
import java.util.List;

import com.cornchipss.chtml.tags.ICustomTag;
import com.cornchipss.chtml.tags.IfTag;
import com.cornchipss.chtml.tags.TemplateTag;
import com.cornchipss.chtml.tags.VarTag;

public class TagRegistry
{
	private static List<ICustomTag> tags = new ArrayList<>();
	
	public static void reigsterAll()
	{
		registerTag(new TemplateTag());
		registerTag(new VarTag());
		registerTag(new IfTag());
	}
	
	public static void registerTag(ICustomTag t)
	{
		tags.add(t);
	}

	public static List<ICustomTag> getTags() { return tags; }
}
