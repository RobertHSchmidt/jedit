package ise.plugin.nav;

import java.awt.event.*;
import java.util.Stack;

import javax.swing.*;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.buffer.JEditBuffer;

//import org.gjt.sp.jedit.buffer.JEditBuffer;

/**
 * 
 * This is the model for the Navigator Plugin. It also creates a ToolBar if
 * necessary.
 * 
 * @ Originally by Dale Anson
 * @ refactored to use model-view classes by Alan Ezust
 * 
 * @version $Revision$
 */
public class Navigator implements ActionListener 
{

	   /** Action command to go to the previous item.  */
	   public final static String BACK = "back";
	   /** Action command to go to the next item.  */
	   public final static String FORWARD = "forward";
	   /** Action command to indicate that it is okay to go back.  */
	   public final static String CAN_GO_BACK = "canGoBack";
	   /** Action command to indicate that it is not okay to go back.  */
	   public final static String CANNOT_GO_BACK = "cannotGoBack";
	   /** Action command to indicate that it is okay to go forward.  */
	   public final static String CAN_GO_FORWARD = "canGoForward";
	   /** Action command to indicate that it is not okay to go forward.  */
	   public final static String CANNOT_GO_FORWARD = "cannotGoForward";

	
	private Stack backStack;

	private Stack forwardStack;

	private DefaultButtonModel backButtonModel;
	
	private DefaultButtonModel forwardButtonModel;

	private Object currentNode = null;

	private int maxStackSize = 512;

	private View view;

	private NavToolBar toolBar;

	public Navigator(View view)
	{
		this(view, "");
	}
	
	/**
	 * Constructor for Navigator
	 * 
	 * @param vw
	 * @param position
	 */
	public Navigator(View vw, String position)
	{
		this.view = vw;

		backButtonModel = new DefaultButtonModel();
		forwardButtonModel = new DefaultButtonModel();
		
	      backButtonModel.setActionCommand( Navigator.BACK );
	      forwardButtonModel.setActionCommand( Navigator.FORWARD );
	      backButtonModel.addActionListener( this );
	      forwardButtonModel.addActionListener( this );
		// set up the history stacks
		backStack = new Stack();
		forwardStack = new Stack();
		clearStacks();

		// create a Nav and make sure the plugin knows about it
		// NavigatorPlugin.addNavigator( view, this );

		// add a mouse listener to the view. Each mouse click on a text
		// area in
		// the view is stored 
		view.getTextArea().getPainter().addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent ce)
			{
				// Buffer b = view.getTextArea().getBuffer(); //
				// for jEdit 4.2
				Buffer b = view.getBuffer(); // for jEdit 4.3
				int cp = view.getTextArea().getCaretPosition();
				update(new NavPosition(b, cp));
				// setPosition(new NavPosition(b, cp));
			}
		});
	      setToolBar();
	}

	public void setToolBar() {
	      if ( jEdit.getBooleanProperty("navigator.showOnToolbar") ) {
		      if (toolBar == null) {
			      toolBar = new NavToolBar(this);
			      view.addToolBar(toolBar);
		      }
		      else {
			      toolBar.setVisible(true);
		      }
	      }
	      else {
		      if (toolBar != null)  {
			      view.removeToolBar(toolBar);
			      toolBar = null;
		      }
	      }
      }
	      
	
	public ButtonModel getBackModel() {
		return backButtonModel;
	}
	
	public ButtonModel getForwardModel() {
		return forwardButtonModel;
	}
	
	public void stop() {
		if (toolBar != null) {
			view.removeToolBar(toolBar);
			toolBar = null;
		}
		
	}
	/**
	 * Updates the stacks and button state based on the given node. Pushes
	 * the node on to the "back" history, clears the "forward" history.
	 * 
	 * @param node
	 *                an object
	 */
	public void update(Object node)
	{
		if (node != currentNode)
		{
			if (currentNode != null)
			{
				backStack.push(currentNode);
				if (backStack.size() > maxStackSize)
					backStack.removeElementAt(0);
			}
			currentNode = node;
			forwardStack.clear();
		}
		setButtonState();
	}

	   /**
	    * The action handler for this class. Actions can be invoked by calling this
	    * method and passing an ActionEvent with one of the action commands defined
	    * in this class (BACK, FORWARD, etc).
	    *
	    * @param ae  the action event to kick a response.
	    */
	   public void actionPerformed( ActionEvent ae ) {
	      if ( ae.getActionCommand().equals( BACK ) ) {
	         goBack();
	      }
	      else if ( ae.getActionCommand().equals( FORWARD ) ) {
	         goForward();
	      }
	      else if ( ae.getActionCommand().equals( CAN_GO_BACK ) ) {
	         backButtonModel.setEnabled( true );
	      }
	      else if ( ae.getActionCommand().equals( CANNOT_GO_BACK ) ) {
	         backButtonModel.setEnabled( false );
	      }
	      else if ( ae.getActionCommand().equals( CAN_GO_FORWARD ) ) {
	         forwardButtonModel.setEnabled( true );
	      }
	      else if ( ae.getActionCommand().equals( CANNOT_GO_FORWARD ) ) {
	         forwardButtonModel.setEnabled( false );
	      }
	   }

	
	/**
	 * Removes an invalid node from the navigation history.
	 * 
	 * @param node
	 *                an invalid node
	 */
	public void remove(Object node)
	{
		backStack.remove(node);
		forwardStack.remove(node);
	}

	public void setMaxHistorySize(int size)
	{
		maxStackSize = size;
	}

	public int getMaxHistorySize()
	{
		return maxStackSize;
	}

	public void clearStacks()
	{
		backStack.clear();
		forwardStack.clear();
		setButtonState();
	}

	/** Sets the state of the navigation buttons. */
	private void setButtonState()
	{
		backButtonModel.setEnabled(!backStack.empty());
		forwardButtonModel.setEnabled(!forwardStack.empty());
	}

	/**
	 * Sets the position of the cursor to the given NavPosition.
	 * 
	 * @param o
	 *                The new NavPosition value
	 */
	public void setPosition(Object o)
	{
		NavPosition np = (NavPosition) o;
		Buffer buffer = np.buffer;
		// JEditBuffer buffer = np.buffer; // for jEdit 4.3
		int caret = np.caret;

		if (buffer.equals(view.getBuffer()))
		{
			// nav in current buffer, just set cursor position
			view.getTextArea().setCaretPosition(caret, true);
			return;
		}

		// check if buffer is open
		Buffer[] buffers = jEdit.getBuffers();
		for (int i = 0; i < buffers.length; i++)
		{
			if (buffers[i].equals(buffer))
			{
				// found it
				view.goToBuffer(buffer);
				view.getTextArea().setCaretPosition(caret, true);
				return;
			}
		}

		// buffer isn't open
		String path = buffer.getPath();
		buffer = jEdit.openFile(view, path);

		if (buffer == null)
		{
			remove(np);
			return; // nowhere to go, maybe the file got deleted?
		}

		if (caret >= buffer.getLength())
		{
			caret = buffer.getLength() - 1;
		}
		if (caret < 0)
		{
			caret = 0;
		}
		try
		{
			view.getTextArea().setCaretPosition(caret, true);
		}
		catch (NullPointerException npe)
		{
			// sometimes Buffer.markTokens throws a NPE here, catch
			// it
			// and silently ignore it.
		}
		view.getTextArea().requestFocus();
	}


	   /** Moves to the previous item in the "back" history.  */
	   public void goBack() {
	      if ( !backStack.empty() ) {
	         if ( currentNode != null ) {
	            forwardStack.push( currentNode );
	            if (forwardStack.size() > maxStackSize)
	               forwardStack.removeElementAt(0);
	         }
	         currentNode = backStack.pop();
	         setPosition( currentNode );
	      }
	      setButtonState();
	   }


	   /** Moves to the next item in the "forward" history.  */
	   public void goForward() {
	      if ( !forwardStack.empty() ) {
	         if ( currentNode != null ) {
	            backStack.push( currentNode );
	            if (backStack.size() > maxStackSize)
	               backStack.removeElementAt(0);
	         }
	         currentNode = forwardStack.pop();
	         setPosition ( currentNode );
	      }
	      setButtonState();
	   }

}
