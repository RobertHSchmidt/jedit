/*
* PythonShell is a Console shell for hosting a Python REPL.
* Copyright (c) 2012 Damien Radtke - www.damienradtke.org
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version
* 2.0 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* General Public License for more details.
*
* For more information, visit http://www.gnu.org/copyleft
*/

package python.shell;

//{{{ Imports
import console.Console;
import console.Output;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import javax.swing.text.AttributeSet;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.ServiceManager;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.util.Log;

import procshell.ProcessShell;
//}}}

public class PythonShell extends ProcessShell {

	private String prompt = ">>> ";
	private String newline;

	/*
 	 * Constructor
 	 */
	public PythonShell() {
		super("Python");
		this.newline = System.getProperty("line.separator");
	}

	/**
	 * Start up Python.
	 */
	protected void init(ConsoleState state, String command) throws IOException {
		Integer selected = jEdit.getIntegerProperty("python-shell.selected-interpreter");
		String cmd = jEdit.getProperty("python-shell.interpreter." + String.valueOf(selected));
		Log.log(Log.DEBUG,this,"Attempting to start Python process: "+cmd);

		LinkedList<String> exec = new LinkedList<String>();
		exec.add(cmd);
		exec.add("-i"); // force python to run in interactive mode
		exec.add("-u"); // force python to use fully unbuffered output
		if (cmd.endsWith("ipython") || cmd.endsWith("ipython.exe")) {
			// if we know this is ipython, disable readline
			exec.add("-noreadline");
		}

		String[] params = jEdit.getProperty("python-shell.parameters." + String.valueOf(selected), "").split(" ");
		for (int i = 0; i<params.length; i++) {
			exec.add(params[i]);
		}

		ProcessBuilder pb = new ProcessBuilder(exec);
		pb.environment().put("TERM", "dumb");
		state.p = pb.start();
		Log.log(Log.DEBUG,this,"Python started.");
	}

	/**
	 * Evaluate text.
	 */
	public void eval(Console console, String str) {
		str = this.clean(str + "\n");
		super.send(console, "eval(\""+str+"\")");
	}

	/**
	 * Execute text.
	 */
	public void exec(Console console, String str) {
		str = this.clean(str + "\n");
		super.send(console, "exec(\""+str+"\")");
	}

	/**
	 * Execute a buffer.
	 */
	public void execBuffer(Console console, Buffer buffer) {
		this.exec(console, buffer.getText(0, buffer.getLength()));
	}

	/**
	 * Execute a file.
	 */
	public void execFile(Console console, String path) {
		super.send(console, "execfile(\""+path.replace("\\", "/")+"\")");
	}

	/**
	 * Called when data is available.
	 */
	protected void onRead(ConsoleState state, String str, Output output) {
		if (str.indexOf(newline) != -1) {
			str = str.substring(str.lastIndexOf(newline)+1);
		}

		if (str.matches(prompt)) {
			state.waiting = false;
			output.commandDone();
		}
	}

	/**
	 * Print python shell info message.
	 */
	public void printInfoMessage(Output output) {
		output.print(null, jEdit.getProperty("msg.python-shell.info-message"));
	}

	/**
	 * Restart the current Python process.
	 */
	public void restart(Console console) {
		this.stop(console);
		this.start(console, console.getOutput(), null);
	}

	private String clean(String str) {
		return str.replace("\n", "\\n").replace("\t", "\\t").replace("\"", "\\\"");
	}
}
