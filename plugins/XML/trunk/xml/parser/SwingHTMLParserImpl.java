/*
 * SwingHTMLParserImpl.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2000, 2001 Slava Pestov
 * Portions copyright (C) 2001 David Walend
 *
 * The XML plugin is licensed under the GNU General Public License, with
 * the following exception:
 *
 * "Permission is granted to link this code with software released under
 * the Apache license version 1.1, for example used by the Xerces XML
 * parser package."
 */

package xml.parser;

//{{{ Imports
import javax.swing.text.html.parser.*;
import javax.swing.text.html.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.tree.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Stack;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.Attributes;
import errorlist.*;
import xml.completion.*;
import xml.*;
//}}}

class SwingHTMLParserImpl implements XmlParser.Impl
{
	//{{{ SwingHTMLParserImpl constructor
	SwingHTMLParserImpl()
	{
		ids = new ArrayList();
		String prop = jEdit.getProperty("mode.html.xml.completion-info");
		if(prop != null)
			info = CompletionInfo.getCompletionInfo(prop);
	} //}}}

	//{{{ parse() method
	public void parse(XmlParser parser, String text)
	{
		this.parser = parser;
		buffer = parser.getBuffer();
		root = new DefaultMutableTreeNode(buffer.getName());

		try
		{
			// XXX
			htmlParser = new DocumentParser(DTD.getDTD("html32"));

			htmlParser.parse(new StringReader(text),new Handler(),true);
		}
		catch(IOException ioe)
		{
			Log.log(Log.ERROR,this,ioe);
			parser.addError(ErrorSource.ERROR,buffer.getPath(),0,
				ioe.toString());
		}
	} //}}}

	//{{{ getElementTree() method
	public TreeNode getElementTree()
	{
		// need to do some cleanup...
		for(int i = 0; i < root.getChildCount(); i++)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(i);
			XmlTag tag = (XmlTag)node.getUserObject();
			if(tag.attributes.getValue("_implied_") != null)
			{
				root.remove(i);

				int j = 0;

				while(node.getChildCount() != 0)
				{
					root.insert((DefaultMutableTreeNode)node.getChildAt(0),i + j);
					j++;
				}

				i--;
			}
		}
		return root;
	} //}}}

	//{{{ getCompletionInfo() method
	public CompletionInfo getCompletionInfo()
	{
		return null;
	} //}}}

	//{{{ getIDs() method
	public ArrayList getIDs()
	{
		return ids;
	} //}}}

	//{{{ Private members

	//{{{ Instance variables
	private XmlParser parser;
	private DocumentParser htmlParser;
	private Buffer buffer;
	private DefaultMutableTreeNode root;
	private CompletionInfo info;
	private ArrayList ids;
	//}}}

	//{{{ attributesToSAX() method
	private Attributes attributesToSAX(MutableAttributeSet a,
		String element, Position pos)
	{
		ElementDecl elementDecl;
		if(info != null)
		{
			elementDecl = (ElementDecl)info.elementHash
				.get(element.toLowerCase());
		}
		else
			elementDecl = null;

		AttributesImpl attrs = new AttributesImpl();
		Enumeration enum = a.getAttributeNames();
		while(enum.hasMoreElements())
		{
			Object attr = enum.nextElement();
			String name = attr.toString();
			String value = a.getAttribute(attr).toString();

			String type = "CDATA";
			if(elementDecl != null)
			{
				ElementDecl.AttributeDecl attrDecl
					= (ElementDecl.AttributeDecl)
					elementDecl.attributeHash
					.get(name.toLowerCase());
				if(attrDecl != null)
				{
					type = attrDecl.type;

					if(type.equals("ID"))
					{
						if(!ids.contains(value))
							ids.add(new IDDecl(value,element,pos));
					}
				}
			}

			attrs.addAttribute(null,name,name,type,value);
		}

		return attrs;
	} //}}}

	//}}}

	//{{{ Handler class
	class Handler extends HTMLEditorKit.ParserCallback
	{
		Stack currentNodeStack = new Stack();

		public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int offset)
		{
			try
			{
				buffer.readLock();

				if(offset > buffer.getLength())
					offset = buffer.getLength();

				Position pos = buffer.createPosition(offset);
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
					new XmlTag(t.toString(),
					pos,attributesToSAX(a,t.toString(),pos)));

				//if(!Boolean.TRUE.equals(a.getAttribute(IMPLIED)))
				{
					if(!currentNodeStack.isEmpty())
					{
						DefaultMutableTreeNode node
							= (DefaultMutableTreeNode)
							currentNodeStack.peek();

						node.insert(newNode,node.getChildCount());
					}
					else
						root.insert(newNode,0);
				}

				currentNodeStack.push(newNode);
			}
			finally
			{
				buffer.readUnlock();
			}
		}

		public void handleEndTag(HTML.Tag t, int offset)
		{
			try
			{
				buffer.readLock();

				if(!currentNodeStack.isEmpty())
				{
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						currentNodeStack.pop();
					XmlTag tag = (XmlTag)node.getUserObject();

					if(offset > buffer.getLength())
						offset = buffer.getLength();

					tag.end = buffer.createPosition(offset);
				}
				else
					/* ? */;
			}
			finally
			{
				buffer.readUnlock();
			}
		}

		public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int offset)
		{
			try
			{
				buffer.readLock();

				if(offset > buffer.getLength())
					offset = buffer.getLength();

				Position pos = buffer.createPosition(offset);

				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
					new XmlTag(t.toString(),
					pos,attributesToSAX(a,t.toString(),pos)));

				if(!currentNodeStack.isEmpty())
				{
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						currentNodeStack.peek();

					node.add(newNode);
				}
				else
					root.add(newNode);
			}
			finally
			{
				buffer.readUnlock();
			}
		}

		public void handleError(String errorMsg, int pos)
		{
			// The Swing parser's error reporting is broken.
			/*try
			{
				buffer.readLock();

				if(pos > buffer.getLength())
					pos = buffer.getLength();
				int line = buffer.getLineOfOffset(pos);

				parser.addError(ErrorSource.ERROR,buffer.getPath(),
					line,errorMsg);
			}
			finally
			{
				buffer.readUnlock();
			}*/
		}
	} //}}}
}
