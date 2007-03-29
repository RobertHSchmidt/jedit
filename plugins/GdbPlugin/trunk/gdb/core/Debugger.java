package gdb.core;

import gdb.breakpoints.Breakpoint;
import gdb.breakpoints.BreakpointList;
import gdb.breakpoints.BreakpointView;
import gdb.breakpoints.GdbBreakpoint;
import gdb.context.StackTrace;
import gdb.core.Parser.GdbResult;
import gdb.core.Parser.ResultHandler;
import gdb.execution.ControlView;
import gdb.launch.LaunchConfiguration;
import gdb.launch.LaunchConfigurationManager;
import gdb.options.GeneralOptionPane;
import gdb.variables.LocalVariables;
import gdb.variables.Variables;
import gdb.variables.Watches;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import debugger.itf.DebuggerTool;
import debugger.itf.IBreakpoint;
import debugger.itf.IData;
import debugger.itf.JEditFrontEnd;

public class Debugger implements DebuggerTool {

	private static Debugger debugger = null;
	
	private JEditFrontEnd frontEnd = null;
	private Parser parser;

	private boolean running = false;

	// Program output
	private static JPanel programOutputPanel = null;
	private static JTextArea programOutputText = null;
	// Gdb output
	private static JPanel gdbOutputPanel = null;
	private static JTextArea gdbOutputText = null;
	// Views
	private ControlView controlView = null;
	private BreakpointView breakpointsPanel = null;
	private LocalVariables localsPanel = null;
	private StackTrace stackTracePanel = null;
	private Watches watchesPanel = null;
	private Variables variablesPanel = null;
	// Command manager
	private CommandManager commandManager = null;

	static private class BreakpointResultHandler implements ResultHandler {
		private GdbBreakpoint bp;
		public BreakpointResultHandler(GdbBreakpoint bp) {
			this.bp = bp;
		}
		public void handle(String msg, GdbResult res) {
			if (! msg.equals("done"))
				return;
			String num = res.getStringValue("bkpt/number");
			if (num != null)
				bp.setNumber(Integer.parseInt(num));
		}
	}
	public IBreakpoint addBreakpoint(String file, int line) {
		GdbBreakpoint bp = new GdbBreakpoint(file, line);
		if (commandManager != null) {
			commandManager.add("-break-insert " + file + ":" + line,
					new BreakpointResultHandler(bp));
		}
		return bp; 
	}

	public IData getData(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public void go() {
		if (! isRunning())
			start();
		else 
			commandManager.add("-exec-continue");
	}

	public void pause() {
		if (isRunning())
			commandManager.add("-exec-interrupt");
	}

	public void quit() {
		if (! isRunning())
			return;
		commandManager.add("-gdb-exit", new ResultHandler() {
			public void handle(String msg, GdbResult res) {
				if (msg.equals("exit")) {
					sessionEnded();
				}
			}
		});
	}

	public void next() {
		if (isRunning())
			commandManager.add("-exec-next");
	}

	public void step() {
		if (isRunning())
			commandManager.add("-exec-step");
	}

	public void finishCurrentFunction() {
		if (isRunning())
			commandManager.add("-exec-finish");
	}

	private void sessionEnded() {
		running = false;
		if (stackTracePanel != null)
			stackTracePanel.sessionEnded();
		if (localsPanel != null)
			localsPanel.sessionEnded();
		if (watchesPanel != null)
			watchesPanel.sessionEnded();
		if (variablesPanel != null)
			variablesPanel.sessionEnded();
		frontEnd.programExited();
	}
	public void start() {
		LaunchConfiguration currentConfig =
			LaunchConfigurationManager.getInstance().getDefault();
		debugger.start(currentConfig.getProgram(),
				currentConfig.getArguments(),
				currentConfig.getDirectory(),
				currentConfig.getEnvironment().split(","));
	}
	public void start(String prog, String args, String cwd, String [] env) {
		String command = jEdit.getProperty(GeneralOptionPane.GDB_PATH_PROP) +
			" --interpreter=mi " + prog;
		//File dir = new File(getBufferDirectory());
		if (cwd == null || cwd.length() == 0)
			cwd = ".";
		File dir = new File(cwd);
		Process p;
		try {
			p = Runtime.getRuntime().exec(command, env, dir);
			running = true;
	        parser = new Parser(this, p);
	        parser.addOutOfBandHandler(new OutOfBandHandler());
			parser.start();
			commandManager = new CommandManager(p, parser);
			commandManager.start();
			// First set up the arguments
			commandManager.add("-exec-arguments " + args);
			// Now set up the breakpoints
			Vector<Breakpoint> bps = BreakpointList.getInstance().getBreakpoints();
			for (int i = 0; i < bps.size(); i++) {
				Breakpoint b = bps.get(i);
				GdbBreakpoint gbp = (GdbBreakpoint)b.getBreakpoint();
				commandManager.add(
						"-break-insert " + gbp.getFile() + ":" + gbp.getLine(),
						new BreakpointResultHandler(gbp));
				if (! b.isEnabled())
					gbp.setEnabled(false);
			}
			commandManager.add("-exec-run");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void stopped(String file, int line) {
		updateStackTrace();
		updateLocals(0);
		updateWatches();
		updateVariables(0);
		frontEnd.setCurrentLocation(file, line);
		
	}

	private void updateWatches() {
		if (watchesPanel != null) {
			watchesPanel.setCommandManager(commandManager);
			watchesPanel.update();
		}
	}
	private void updateVariables(int frame) {
		if (variablesPanel != null) {
			variablesPanel.setCommandManager(commandManager);
			variablesPanel.update(frame);
		}
	}
	private void updateStackTrace() {
		if (stackTracePanel != null) {
			stackTracePanel.setCommandManager(commandManager);
			stackTracePanel.setParser(parser);
			stackTracePanel.update();
		}
	}
	public void breakpointHit(int bkptno, String file, int line) {
		String msg = "Breakpoint " + bkptno + " hit";
		if (file != null)
			msg = msg + ", at " + file + ":" + line + ".";
		System.err.println(msg);
		JOptionPane.showMessageDialog(null, msg);
	}

	public void setFrontEnd(JEditFrontEnd frontEnd) {
		this.frontEnd = frontEnd;
	}

	private class BreakpointHitHandler implements ResultHandler {
		int bkptno;
		BreakpointHitHandler(int bkptno) {
			this.bkptno = bkptno;
		}
		public void handle(String msg, GdbResult res) {
			String file = res.getStringValue("fullname");
			int line = 0;
			if (file != null) {
				line = Integer.parseInt(res.getStringValue("line"));
			}
			breakpointHit(bkptno, file, line);
			stopped(file, line);
		}
	}
	private class StoppedHandler implements ResultHandler {
		public void handle(String msg, GdbResult res) {
			String file = res.getStringValue("fullname");
			int line = 0;
			if (file != null) {
				line = Integer.parseInt(res.getStringValue("line"));
			}
			stopped(file, line);
		}
	}
	private class OutOfBandHandler implements ResultHandler {
		public void handle(String msg, GdbResult res) {
			final String getCurrentPosition = "-file-list-exec-source-file";
			String reason = res.getStringValue("reason");
			if (reason.equals("breakpoint-hit")) {
				int bkptno = Integer.parseInt(res.getStringValue("bkptno"));
				commandManager.add(getCurrentPosition, new BreakpointHitHandler(bkptno));
			} else if (reason.startsWith("exited")) {
				System.err.println("Exited");
				sessionEnded();
			} else {
				commandManager.add(getCurrentPosition, new StoppedHandler());
			}
		}
	}

	public JEditFrontEnd getFrontEnd() {
		return frontEnd;
	}
	public void updateLocals(int frame) {
		if (localsPanel != null) {
			localsPanel.setCommandManager(commandManager);
			localsPanel.update(frame);
		}
	}
	public void frameSelected(int level) {
		updateLocals(level);
		updateVariables(level);
	}

	public JPanel showControlPanel(View view) {
		if (controlView == null)
			controlView = new ControlView();
		return controlView;
	}
	public JPanel showProgramOutput(View view) {
		if (programOutputPanel == null)	{
			programOutputPanel = new JPanel(new BorderLayout());
			programOutputText = new JTextArea();
			programOutputPanel.add(new JScrollPane(programOutputText));
		}
		return programOutputPanel;
	}
	public JPanel showGdbOutput(View view) {
		if (gdbOutputPanel == null)	{
			gdbOutputPanel = new JPanel(new BorderLayout());
			gdbOutputText = new JTextArea();
			gdbOutputPanel.add(new JScrollPane(gdbOutputText));
		}
		return gdbOutputPanel;
	}
	public JPanel showBreakpoints(View view) {
		if (breakpointsPanel == null)
			breakpointsPanel = new BreakpointView();
		return breakpointsPanel;
	}
	public JPanel showLocals(View view) {
		if (localsPanel == null)
			localsPanel = new LocalVariables();
		return localsPanel;
	}
	public JPanel showWatches(View view) {
		if (watchesPanel == null)
			watchesPanel = new Watches();
		return watchesPanel;
	}
	public JPanel showVariables(View view) {
		if (variablesPanel == null)
			variablesPanel = new Variables();
		return variablesPanel;
	}
	public JPanel showStackTrace(View view) {
		if (stackTracePanel == null)
			stackTracePanel = new StackTrace();
		return stackTracePanel;
	}
	public void gdbRecord(String line)
	{
		if (gdbOutputText == null)
			showGdbOutput(jEdit.getActiveView());
		gdbOutputText.append(line);
	}
	public void programRecord(String line)
	{
		if (programOutputText == null)
			showProgramOutput(jEdit.getActiveView());
		programOutputText.append(line);
	}
	public static Debugger getInstance() {
		if (debugger  == null)
			debugger = new Debugger();
		return debugger;
	}

	public boolean isRunning() {
		return running;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

}
