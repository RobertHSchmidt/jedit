/*
 * Chunk.java - A syntax token with extra information required for painting it
 * on screen
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2002 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit.syntax;

//{{{ Imports
import javax.swing.text.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.*;
import java.util.Map;
import java.util.WeakHashMap;

import org.gjt.sp.jedit.Debug;
//}}}

/**
 * A syntax token with extra information required for painting it
 * on screen.
 * @since jEdit 4.1pre1
 */
public class Chunk extends Token
{
	//{{{ paintChunkList() method
	/**
	 * Paints a chunk list.
	 * @param chunks The chunk list
	 * @param gfx The graphics context
	 * @param x The x co-ordinate
	 * @param y The y co-ordinate
	 * @return The width of the painted text
	 * @since jEdit 4.2pre1
	 */
	public static float paintChunkList(Chunk chunks,
		Graphics2D gfx, float x, float y, boolean glyphVector)
	{
		Rectangle clipRect = gfx.getClipBounds();

		float _x = 0.0f;

		while(chunks != null)
		{
			// only paint visible chunks
			if(x + _x + chunks.width > clipRect.x
				&& x + _x < clipRect.x + clipRect.width)
			{
				// Useful for debugging purposes
				if(Debug.CHUNK_PAINT_DEBUG)
				{
					gfx.draw(new Rectangle2D.Float(x + _x,y - 10,
						chunks.width,10));
				}

				if(chunks.accessable && chunks.visible)
				{
					gfx.setFont(chunks.style.getFont());
					gfx.setColor(chunks.style.getForegroundColor());

					if(glyphVector && chunks.gv != null)
						gfx.drawGlyphVector(chunks.gv,x + _x,y);
					else if(chunks.str != null)
					{
						gfx.drawString(chunks.str,
							(int)(x + _x),(int)y);
					}
				}
			}

			_x += chunks.width;
			chunks = (Chunk)chunks.next;
		}

		return _x;
	} //}}}

	//{{{ paintChunkBackgrounds() method
	/**
	 * Paints the background highlights of a chunk list.
	 * @param chunks The chunk list
	 * @param gfx The graphics context
	 * @param x The x co-ordinate
	 * @param y The y co-ordinate
	 * @return The width of the painted backgrounds
	 * @since jEdit 4.2pre1
	 */
	public static float paintChunkBackgrounds(Chunk chunks,
		Graphics2D gfx, float x, float y)
	{
		Rectangle clipRect = gfx.getClipBounds();

		float _x = 0.0f;

		FontMetrics forBackground = gfx.getFontMetrics();

		int ascent = forBackground.getAscent();
		int height = forBackground.getHeight();

		while(chunks != null)
		{
			// only paint visible chunks
			if(x + _x + chunks.width > clipRect.x
				&& x + _x < clipRect.x + clipRect.width)
			{
				if(chunks.accessable)
				{
					//{{{ Paint token background color if necessary
					Color bgColor = chunks.background;
					if(bgColor != null)
					{
						gfx.setColor(bgColor);

						gfx.fill(new Rectangle2D.Float(
							x + _x,y - ascent,
							_x + chunks.width - _x,
							height));
					} //}}}
				}
			}

			_x += chunks.width;
			chunks = (Chunk)chunks.next;
		}

		return _x;
	} //}}}

	//{{{ offsetToX() method
	/**
	 * Converts an offset in a chunk list into an x co-ordinate.
	 * @param chunks The chunk list
	 * @param offset The offset
	 * @since jEdit 4.1pre1
	 */
	public static float offsetToX(Chunk chunks, int offset)
	{
		if(chunks != null && offset < chunks.offset)
		{
			throw new ArrayIndexOutOfBoundsException(offset + " < "
				+ chunks.offset);
		}

		float x = 0.0f;

		while(chunks != null)
		{
			if(chunks.accessable && offset < chunks.offset + chunks.length)
				return x + chunks.offsetToX(offset - chunks.offset);

			x += chunks.width;
			chunks = (Chunk)chunks.next;
		}

		return x;
	} //}}}

	//{{{ xToOffset() method
	/**
	 * Converts an x co-ordinate in a chunk list into an offset.
	 * @param chunks The chunk list
	 * @param x The x co-ordinate
	 * @param round Round up to next letter if past the middle of a letter?
	 * @return The offset within the line, or -1 if the x co-ordinate is too
	 * far to the right
	 * @since jEdit 4.1pre1
	 */
	public static int xToOffset(Chunk chunks, float x, boolean round)
	{
		float _x = 0.0f;

		while(chunks != null)
		{
			if(chunks.accessable && x < _x + chunks.width)
				return chunks.xToOffset(x - _x,round);

			_x += chunks.width;
			chunks = (Chunk)chunks.next;
		}

		return -1;
	} //}}}

	//{{{ Instance variables
	public boolean accessable;
	public boolean visible;
	public boolean initialized;

	// set up after init()
	public SyntaxStyle style;
	// this is either style.getBackgroundColor() or
	// styles[defaultID].getBackgroundColor()
	public Color background;
	public float width;
	public GlyphVector gv;
	public String str;
	//}}}

	//{{{ Chunk constructor
	public Chunk(float width, int offset, ParserRuleSet rules)
	{
		super(Token.NULL,offset,0,rules);
		this.width = width;
	} //}}}

	//{{{ Chunk constructor
	public Chunk(byte id, int offset, int length, ParserRuleSet rules,
		SyntaxStyle[] styles, byte defaultID)
	{
		super(id,offset,length,rules);
		accessable = true;
		style = styles[id];
		background = style.getBackgroundColor();
		if(background == null)
			background = styles[defaultID].getBackgroundColor();
	} //}}}

	//{{{ getPositions() method
	public final float[] getPositions()
	{
		if(gv == null)
			return null;

		if(positions == null)
			positions = gv.getGlyphPositions(0,length,null);

		return positions;
	} //}}}

	//{{{ offsetToX() method
	public final float offsetToX(int offset)
	{
		if(!visible)
			return 0.0f;
		else
			return getPositions()[offset * 2];
	} //}}}

	//{{{ xToOffset() method
	public final int xToOffset(float x, boolean round)
	{
		if(!visible)
		{
			if(round && width - x < x)
				return offset + length;
			else
				return offset;
		}
		else
		{
			float[] pos = getPositions();

			for(int i = 0; i < length; i++)
			{
				float glyphX = pos[i*2];
				float nextX = (i == length - 1
					? width : pos[i*2+2]);

				if(nextX > x)
				{
					if(!round || nextX - x > x - glyphX)
						return offset + i;
					else
						return offset + i + 1;
				}
			}
		}

		// wtf?
		return -1;
	} //}}}

	//{{{ init() method
	public void init(Segment seg, TabExpander expander, float x,
		FontRenderContext fontRenderContext)
	{
		initialized = true;

		if(!accessable)
		{
			// do nothing
		}
		else if(length == 1 && seg.array[seg.offset + offset] == '\t')
		{
			visible = false;
			float newX = expander.nextTabStop(x,offset + length);
			width = newX - x;
		}
		else
		{
			visible = true;

			str = new String(seg.array,seg.offset + offset,length);
			
			Rectangle2D logicalBounds;
			if(str.length() < CHUNK_CACHE_LIMIT)
			{
				// if the string length is smaller than the CHUNK_CACHE_LIMIT we look in the map if
				// the GlyphVector already exists. If not it will be added.
				gv = (GlyphVector) glyphVectorMap.get(str);
				if(gv == null)
				{
					gv = style.getFont().createGlyphVector(
						fontRenderContext, str);
					glyphVectorMap.put(str, gv);
				}
				
				logicalBounds = (Rectangle2D) logicalBoundsMap.get(str);
				if(logicalBounds == null )
				{
					logicalBounds = gv.getLogicalBounds();
					logicalBoundsMap.put(str,logicalBounds);
				}
			}
			else
			{
				gv = style.getFont().createGlyphVector(
					fontRenderContext, str);
				logicalBounds = gv.getLogicalBounds();
			}
			width = (float)logicalBounds.getWidth();
		}
	} //}}}

	//{{{ Private members
	private float[] positions;

	/**
	 * The cache for GlyphVector. The key is the String
	 * and the value the GlyphVector for this String
	 */
	private static final Map glyphVectorMap = new WeakHashMap();
	/**
	 * The cache for bounds. The key is the String
	 * and the value the GlyphVector for this String
	 */
	private static final Map logicalBoundsMap = new WeakHashMap();
	
	/** the limit size of the Chunk data that will be cached in the WeakHashMaps. */
	public static int CHUNK_CACHE_LIMIT = 10;
	//}}}
}
