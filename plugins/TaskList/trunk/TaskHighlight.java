/*
 * TaskHighlight.java - TaskList plugin
 * Copyright (C) 2001 Oliver Rutherfurd
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
 *
 * $Id$
 */

import java.awt.event.*;
import java.awt.*;

import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.jedit.*;

import org.gjt.sp.util.Log;

import java.util.Hashtable;
import javax.swing.text.Segment;

/**
 * A class implementing jEdit's TextAreaHighlight interface
 * that, when enabled, draws a wavy line underscoring a task item
 * appearing in the text of a buffer.
 */
public class TaskHighlight implements TextAreaHighlight
{

	/**
	 * Returns whether highlighting of task items is currently enabled.
	 *
	 * @return the value true or false indicating whther highlighting
	 * is enabled
	 */
	public boolean isEnabled()
	{
		return highlightEnabled;
	}


	/**
	 * Set whether highlighting of task items is enabled; does not
	 * redraw the text area after a change in state.
	 *
	 * @param the new state for task highlighting
	 */
	public void setEnabled(boolean enabled)
	{
		highlightEnabled = enabled;
	}

	/**
	 * Called by the application after the highlight painter has been added.
	 * @param textArea The text area
	 * @param next The painter this one should delegate to
	 */
	public void init(JEditTextArea textArea, TextAreaHighlight next)
	{
		this.textArea = textArea;
		this.next = next;
		this.highlightEnabled = jEdit.getBooleanProperty("tasklist.highlight.tasks");
		this.seg = new Segment();
	}

	/**
	 * Paints the highlight (if enabled) and delgates further
	 * highlighting to the next highlight painter.
	 * @param gfx The graphics context
	 * @param line The virtual line number
	 * @param y The y co-ordinate of the line
	 */
	public void paintHighlight(Graphics gfx, int line, int y)
	{
		if(highlightEnabled)
		{
			int lineCount = textArea.getVirtualLineCount();
			Buffer buffer = textArea.getBuffer();
			if(line >= lineCount || !buffer.isLoaded())
				return;

			Hashtable taskMap =
				TaskListPlugin.requestTasksForBuffer(buffer);

			int physicalLine = buffer.virtualToPhysical(line);

			if(taskMap != null)
			{
				Task task;
				Integer _line = new Integer(physicalLine);
				if(!buffer.IsDirty())
				{
					task =  (Task)taskMap.get(_line);
				}
				else
				{
					Enumeration enum = taskMap.elements();
					while(enum.hasMoreElement())
					{
						Task _task = (Task)enum.nextElement();
						if(_task.getLineNumber() == _line.intValue())
						{
							task = _task;
							break;
						}
					}
				}
				if(task != null)
				{
					underlineTask(task, gfx, physicalLine, y);
				}
			}
		}

		if(next != null)
			next.paintHighlight(gfx, line, y);
	}

	/**
	 * Returns the tool tip to display at the specified location.
	 * This implementation delegates performance to the next highlight
	 * painter.
	 * @param evt The mouse event
	 */
	public String getToolTipText(MouseEvent evt)
	{
		if(this.next == null)
			return null;

		return this.next.getToolTipText(evt);
	}

	/**
	 * The textArea on which the highlight will be drawn.
	 */
	private JEditTextArea textArea;

	/**
	 * The highlight to which the TaskHighlight object will delegate
	 * further drawing.
	 */
	private TextAreaHighlight next;

	/**
	 * A flag indicating whether highlighting of task items
	 * is currently enabled.
	 */
	private boolean highlightEnabled;

	/**
	 *
	 * A portion of text to be highlighted.
	 */
	private Segment seg;


	/**
	 * Implements underlining of task items through a call to
	 * paintWavyLine()
	 *
	 * @param task the Task that is the subject of highlighting
	 * @param gfx The graphics context
	 * @param line The virtual line number
	 * @param y The y co-ordinate of the line
	 */
	private void underlineTask(Task task,
		Graphics gfx, int line, int y)
	{
		int start = task.getStartOffset();
		int end = task.getEndOffset();

		//Log.log(Log.DEBUG, TaskHighlight.class,
		//	"line=" + line + ",y=" + y + ",start=" +
		//	start + ",end=" + end + ",task=" + task);//##

		start = textArea.offsetToX(line, start);
		end = textArea.offsetToX(line, end);

		gfx.setColor(TaskListPlugin.getHighlightColor());
		paintWavyLine(gfx, y, start, end);
	}

	/**
	 * Draws a wavy line at the indicated coordinates
	 *
	 * @param gfx The graphics context
	 * @param y The y-coordinate representing the lower bound of the wavy line
	 * @param start The x-coordinate of the start of the line
	 * @param end The x-coordinate of the end of the line
	 */
	private void paintWavyLine(Graphics gfx, int y, int start, int end)
	{
		y += textArea.getPainter().getFontMetrics().getHeight();

		for(int i = start; i < end; i+= 6)
		{
			gfx.drawLine(i,y + 3,i + 3,y + 1);
			gfx.drawLine(i + 3,y + 1,i + 6,y + 3);
		}
	}

}
