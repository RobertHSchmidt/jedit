package org.jedit.plugins.columnruler.marks;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.Line2D;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.util.Log;

import org.jedit.plugins.columnruler.DynamicMark;
import org.jedit.plugins.columnruler.ColumnRuler;
import org.jedit.plugins.columnruler.ColumnRulerPlugin;


/**
 *  Mark which follows the wrap column of the current buffer. Dragging the wrap
 *  mark will set the buffer's line length. The wrap mark paints its guide with
 *  a dashed line.
 *
 * @author     mace
 * @version    $Revision: 1.3 $ modified $Date: 2006-10-10 19:40:47 $ by
 *      $Author: k_satoda $
 */
public class WrapMark extends DynamicMark implements EBComponent {
	public WrapMark() {
		super("Wrap", "options.columnruler.marks.wrap");
		setSize(3);
	}

	public void handleMessage(EBMessage msg) {
		super.handleMessage(msg);
		
		if (msg instanceof PropertiesChanged) {
			if (msg.getSource() instanceof JEditBuffer) {
				updateRulersViewing((JEditBuffer) msg.getSource());
			}
		}
		
		if (msg instanceof EditPaneUpdate) {
			EditPaneUpdate epu = (EditPaneUpdate) msg;
			if (epu.getWhat().equals(epu.BUFFER_CHANGED) || epu.getWhat().equals(epu.CREATED)) {
				updateRulersViewing(epu.getEditPane().getTextArea());
			}
		}
		
		if (msg instanceof BufferUpdate) {
			BufferUpdate bu = (BufferUpdate) msg;
			if (bu.getWhat().equals(bu.CREATED) || bu.getWhat().equals(bu.LOADED) || bu.getWhat().equals(bu.PROPERTIES_CHANGED)) {
				updateRulersViewing(bu.getBuffer());
			}
		}
		
	}
	
	private void updateRulersViewing(TextArea textArea) {
		ColumnRuler ruler = ColumnRulerPlugin.getColumnRulerForTextArea(textArea);
		if (ruler == null) {
			Log.log(Log.DEBUG, this, "ruler not found");
			return;
		}
		JEditBuffer buffer = textArea.getBuffer();
		positionMap.put(ruler, buffer.getIntegerProperty("maxLineLen", 0));
		setVisible(!buffer.getStringProperty("wrap").equals("none"));
		ruler.repaint();
		if (isGuideVisible()) {
			textArea.repaint();
		}
	}
	
	private void updateRulersViewing(JEditBuffer buffer) {
		for (View view : jEdit.getViews()) {
			for (EditPane editPane : view.getEditPanes()) {
				if (editPane.getBuffer().equals(buffer)) {
					updateRulersViewing(editPane.getTextArea());
				}
			}
		}
	}

	// {{{ drawGuide
	/**
	 * Draws the wrap guide as a dashed line.
	 */
	public void drawGuide(Graphics2D gfx, ColumnRuler ruler) {
		gfx.setColor(getColor());
		int hScroll = ruler.getTextArea().getHorizontalOffset();
		double x = getPositionOn(ruler) * ruler.getCharWidth() + hScroll;
		Line2D guide = new Line2D.Double();
		double dashLength = 2 * ruler.getLineHeight() / 3;
		double dashSpacing = 2 * ruler.getLineHeight() / 3;
		double yOffset = ruler.getTextArea().getPhysicalLineOfScreenLine(0)*ruler.getLineHeight() % (dashLength+dashSpacing);
		if (yOffset > dashSpacing) {
			guide.setLine(x,0,x,yOffset-dashSpacing);
			gfx.draw(guide);
		}
		for (double y = yOffset; y < ruler.getTextArea().getHeight(); y += dashLength +dashSpacing) {
			guide.setLine(x, y, x, y + dashLength);
			gfx.draw(guide);
		}
	} //}}}

	//{{{ Accessors and Mutators
	
	public void setPositionOn(ColumnRuler ruler, int col) {
		super.setPositionOn(ruler, col);
		JEditBuffer buffer = ruler.getTextArea().getBuffer();
		buffer.setIntegerProperty("maxLineLen", col);
		ruler.getTextArea().propertiesChanged();
		if (buffer instanceof Buffer) {
			((Buffer) buffer).propertiesChanged();
		}
	}

	public void setGuideVisible(boolean b) {
		super.setGuideVisible(b);
		if (b) {
			jEdit.setBooleanProperty("view.wrapGuide", false);
		}
	}
	
	public Color getColor() {
		return jEdit.getActiveView().getTextArea().getPainter().getWrapGuideColor();
	}

	//}}}

}

