/*
 * ElementDecl.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001 Slava Pestov
 *
 * The XML plugin is licensed under the GNU General Public License, with
 * the following exception:
 *
 * "Permission is granted to link this code with software released under
 * the Apache license version 1.1, for example used by the Xerces XML
 * parser package."
 */

package xml.parser;

import java.util.*;
import org.gjt.sp.jedit.MiscUtilities;
import xml.completion.*;

public class ElementDecl
{
	public String name;
	public boolean empty;
	public boolean any;
	public boolean html;

	public ArrayList attributes;
	public HashMap attributeHash;

	private HashSet content;

	//{{{ ElementDecl constructor
	public ElementDecl(String name, String content, boolean html)
	{
		this.name = name;
		this.html = html;

		if(content != null)
			setContent(content);

		attributes = new ArrayList();
		attributeHash = new HashMap();
	} //}}}

	//{{{ setContent() method
	public void setContent(String content)
	{
		if(content.equals("EMPTY"))
			empty = true;
		else if(content.equals("ANY"))
			any = true;
		else
		{
			this.content = new HashSet();

			StringTokenizer st = new StringTokenizer(content,
				"(?*+|,) \t\n");
			while(st.hasMoreTokens())
			{
				String element = st.nextToken();
				if(element.equals("#PCDATA"))
					continue;

				this.content.add(element);
			}
		}
	} //}}}

	//{{{ getChildElements() method
	public ArrayList getChildElements(CompletionInfo info)
	{
		ArrayList children = new ArrayList(100);

		if(any)
		{
			for(int i = 0; i < info.elements.size(); i++)
			{
				children.add(info.elements.get(i));
			}
		}
		else
		{
			for(int i = 0; i < info.elementsAllowedAnywhere.size(); i++)
			{
				children.add(info.elementsAllowedAnywhere.get(i));
			}

			Iterator iter = content.iterator();
			while(iter.hasNext())
			{
				ElementDecl decl = (ElementDecl)info.elementHash
					.get(iter.next());
				if(decl != null)
					children.add(decl);
			}

			MiscUtilities.quicksort(children,new ElementDecl.Compare());
		}

		return children;
	} //}}}

	//{{{ getAttribute() method
	public AttributeDecl getAttribute(String name)
	{
		return (AttributeDecl)attributeHash.get(name);
	} //}}}

	//{{{ addAttribute() method
	public void addAttribute(AttributeDecl attribute)
	{
		attributeHash.put(attribute.name,attribute);

		for(int i = 0; i < attributes.size(); i++)
		{
			AttributeDecl attr = (AttributeDecl)attributes.get(i);
			if(attr.name.compareTo(attribute.name) > 0)
			{
				attributes.add(i,attribute);
				return;
			}
		}

		attributes.add(attribute);
	} //}}}

	//{{{ toString() method
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("<element name=\"");
		buf.append(name);
		buf.append('"');

		buf.append("\ncontent=\"");

		if(empty)
			buf.append("EMPTY");
		else
		{
			buf.append('(');

			Iterator iter = content.iterator();
			while(iter.hasNext())
			{
				buf.append(iter.next());
				if(iter.hasNext())
					buf.append('|');
			}

			buf.append(')');
		}

		buf.append('"');

		if(html)
			buf.append(" html=\"true\"");

		if(attributes.size() == 0)
			buf.append(" />");
		else
		{
			buf.append(">\n");
			for(int i = 0; i < attributes.size(); i++)
			{
				buf.append(attributes.get(i));
				buf.append('\n');
			}
			buf.append("</element>");
		}

		return buf.toString();
	} //}}}

	//{{{ AttributeDecl class
	public static class AttributeDecl
	{
		public String name;
		public String value;
		public ArrayList values;
		public String type;
		public boolean required;

		public AttributeDecl(String name, String value, ArrayList values,
			String type, boolean required)
		{
			this.name = name;
			this.value = value;
			this.values = values;
			this.type = type;
			this.required = required;
		}

		public String toString()
		{
			StringBuffer buf = new StringBuffer("<attribute name=\"");
			buf.append(name);
			buf.append('"');

			if(value != null)
			{
				buf.append(" value=\"");
				buf.append(value);
				buf.append('"');
			}

			buf.append(" type=\"");
			buf.append(type);
			buf.append('"');

			if(required)
				buf.append(" required=\"true\"");

			buf.append(" />");
			return buf.toString();
		}
	} //}}}

	//{{{ Compare class
	public static class Compare implements MiscUtilities.Compare
	{
		public int compare(Object obj1, Object obj2)
		{
			return MiscUtilities.compareStrings(
				((ElementDecl)obj1).name,
				((ElementDecl)obj2).name,true);
		}
	} //}}}
}
