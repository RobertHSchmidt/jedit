package columnruler;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.geom.*;

import org.gjt.sp.jedit.*;

/**
 * Description of the Class
 *
 * @author     mace
 * @created    June 8, 2003
 * @modified   $Date: 2004-02-09 20:04:05 $ by $Author: bemace $
 * @version    $Revision: 1.6 $
 */
public class Mark implements Transferable {
	static final DataFlavor MARK_FLAVOR = new DataFlavor(Mark.class,"ColumnRuler.Mark");

	private String _name;
	private Color _color;
	protected int _column = 0;
	private int size = 1;
	private boolean _visible = true;
	protected boolean guide = false;

	public Mark(String name) {
		_name = name;
	}

	public Mark(String name, Color c) {
		this(name);
		_color = c;
	}

	public void activate(ColumnRuler ruler) {}

	public void deactivate() {}

	/**
	 * Subclasses can override this to draw fancier guides.
	 */
	public void drawGuide(Graphics2D gfx, ColumnRuler ruler) {
		int hScroll = ruler.getTextArea().getHorizontalOffset();
		double x = getColumn()*ruler.charWidth + hScroll;
		Line2D guide;
		guide = new Line2D.Double(x,0,x,ruler.getTextArea().getHeight());
		gfx.setColor(getColor());
		gfx.draw(guide);
	}

	//{{{ Transferable impl
	public Object getTransferData(DataFlavor flavor) {
		return this;
	}

	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] df = new DataFlavor[1];
		df[0] = MARK_FLAVOR;
		return df;
	}

	public boolean isDataFlavorSupported(DataFlavor f) {
		if (f.getRepresentationClass().equals(Mark.class)) {
			return true;
		}
		return false;
	}
	//}}}

	//{{{ Accessors + Mutators
	public void setColumn(int col) {
		_column = col;
	}

	/**
	 * Shows/Hides this mark.
	 */
	public void setVisible(boolean b) {
		_visible = b;
	}

	/**
	 * Sets the width of the mark.
	 */
	public void setSize(int s) {
		size = s;
	}

	/**
	 * Turns guide drawing of/off.
	 */
	public void setGuideVisible(boolean b) {
		guide = b;
	}

	/**
	 * Gets the name of this marker, used in tooltips.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Gets the color to use for the mark and its guide.
	 */
	public Color getColor() {
		return _color;
	}

	/**
	 * Gets the current column of this mark.
	 */
	public int getColumn() {
		return _column;
	}

	/**
	 * Gets the width of this mark.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Returns true if the mark is currently visible.
	 */
	public boolean isVisible() {
		return _visible;
	}

	/**
	 * Returns true if the guide for this mark is visible.
	 */
	public boolean isGuideVisible() {
		return guide;
	}
	//}}}
}

