package com.cornchipss.chtml.core;

import com.cornchipss.chtml.core.tags.IfTag;
import com.cornchipss.chtml.core.tags.TemplateTag;
import com.cornchipss.chtml.core.tags.VarTag;
import com.cornchipss.chtml.plugins.CustomHTMLPlugin;

import static com.cornchipss.chtml.TagRegistry.registerTag;

public class CustomHTMLCorePlugin extends CustomHTMLPlugin
{
	@Override
	public void enable()
	{
		registerTag(new VarTag());
		registerTag(new TemplateTag());
		registerTag(new IfTag());
	}
}
