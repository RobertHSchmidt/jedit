package ctagsinterface.jedit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.TextAreaExtension;
import org.gjt.sp.jedit.visitors.JEditVisitorAdapter;

import ctagsinterface.main.CtagsInterfacePlugin;
import ctagsinterface.main.Tag;
import ctagsinterface.options.GeneralOptionPane;

public class TagTooltip extends TextAreaExtension
{
	private JEditTextArea textArea;
	private static Attacher attacher;
	
	{
	}
	
	public TagTooltip(JEditTextArea textArea)
	{
		this.textArea = textArea;
	}
	
	public static void start()
	{
		attachToAll();
		attacher = new Attacher();
		EditBus.addToBus(attacher);
	}
	
	private static void attachToAll()
	{
		jEdit.visit(new JEditVisitorAdapter() {
			public void visit(JEditTextArea textArea) {
				attachToTextArea(textArea);
			}
		});
	}

	private static void attachToTextArea(JEditTextArea ta) {
		if (ta.getClientProperty("TagTooltip") != null)
			return;
		TagTooltip ext = new TagTooltip(ta);
		ta.getPainter().addExtension(ext);
		ta.putClientProperty("TagTooltip", ext);
	}
	
	public static void stop()
	{
		EditBus.removeFromBus(attacher);
		attacher = null;
		detachFromAll();
	}
	
	public static void detachFromAll()
	{
		jEdit.visit(new JEditVisitorAdapter() {
			public void visit(JEditTextArea textArea) {
				detachFromTextArea(textArea);
			}
		});
	}

	private static void detachFromTextArea(JEditTextArea ta) {
		TagTooltip ext = (TagTooltip) ta.getClientProperty("TagTooltip");
		if (ext != null)
		{
			ta.getPainter().removeExtension(ext);
			ta.putClientProperty("TagTooltip", null);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String getToolTipText(final int x, final int y) {
		int offset = textArea.xyToOffset(x, y);
		JEditBuffer buffer = textArea.getBuffer();
		int line = buffer.getLineOfOffset(offset);
		int index = offset - buffer.getLineStartOffset(line);
		final String tag = CtagsInterfacePlugin.getTagAt(textArea, line,
			index);
		if ((tag == null) || (tag.length() == 0))
			return null;
		SwingWorker w = new SwingWorker<Void,Void>() {
			String tooltip;
			@Override
			protected void done() {
				JToolTip tt = textArea.createToolTip();
				tt.setTipText(tooltip);
				PopupFactory factory = PopupFactory.getSharedInstance();
				int x1 = textArea.getLocationOnScreen().x + x;
				int y1 = textArea.getLocationOnScreen().y + y;
				final Popup popup = factory.getPopup(textArea, tt, x1, y1);
				popup.show();
				int d = ToolTipManager.sharedInstance().getDismissDelay();
				final ActionListener dismiss = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						popup.hide();
					}
				};
				final Timer t = new Timer(d, dismiss);
				t.restart();
				textArea.getPainter().addMouseMotionListener(
					new MouseMotionListener() {
						private void dismiss() {
							t.stop();
							dismiss.actionPerformed(null);
						}
						public void mouseDragged(MouseEvent e) {
							dismiss();
						}
						public void mouseMoved(MouseEvent e) {
							dismiss();
						}
					});
			}
			@Override
			protected Void doInBackground() throws Exception {
				Vector<Tag> tags = CtagsInterfacePlugin.queryScopedTag(
						textArea.getView(), tag);
				if (tags == null || tags.isEmpty())
					return null;
				StringBuffer sb = new StringBuffer("<html>");
				boolean first = true;
				for (Tag t: tags) {
					if (! first)
						sb.append("<br>");
					else
						first = false;
					sb.append(getTagString(t));
				}
				sb.append("</html>");
				tooltip = sb.toString();
				return null;
			}
		};
		w.execute();
		return null;
	}
	
	private String getTagString(Tag tag)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(tag.getName());
		String signature = tag.getExtension("signature");
		if (signature != null && signature.length() > 0)
			sb.append(signature + " ");
		StringBuffer details = new StringBuffer();
		String namespace = tag.getNamespace();
		if (namespace != null && namespace.length() > 0)
			details.append(namespace);
		String kind = tag.getKind();
		if (kind != null && kind.length() > 0)
		{
			if (details.length() > 0)
				details.append(" ");
			details.append(tag.getKind());
		}
		if (details.length() > 0)
			sb.append(" (" + details.toString() + ")");
		return sb.toString();
	}

	private static class Attacher implements EBComponent
	{
		public void handleMessage(EBMessage message) {
			if (message instanceof EditPaneUpdate)
			{
				if (! GeneralOptionPane.getShowTooltips())
					return;
				EditPaneUpdate epu = (EditPaneUpdate) message;
				if (epu.getWhat().equals(EditPaneUpdate.CREATED))
					attachToTextArea(epu.getEditPane().getTextArea());
				else if (epu.getWhat().equals(EditPaneUpdate.DESTROYED))
					detachFromTextArea(epu.getEditPane().getTextArea());
			}
			else if (message instanceof PropertiesChanged)
			{
				if (GeneralOptionPane.getShowTooltips())
					attachToAll();
				else
					detachFromAll();
			}
		}
	}
}
