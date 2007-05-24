package gdb.output;

import gdb.core.Debugger;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.gjt.sp.jedit.jEdit;

import console.Console;
import console.Output;
import debugger.jedit.Plugin;

public class ProgramShell extends BaseShell {

	static final String PREFIX = Plugin.OPTION_PREFIX;
	static final String MI_SHELL_INFO_MSG_PROP = PREFIX + "program_shell_info_msg";
	public static String NAME = "Program";
	private OutputStreamWriter writer = null;
	
	public ProgramShell() {
		super(NAME);
	}
	
	public ProgramShell(String arg0) {
		super(arg0);
	}

	private OutputStreamWriter getWriter() {
		if (writer == null)
	        writer = new OutputStreamWriter(
	        		Debugger.getInstance().getGdbProcess().getOutputStream());
		return writer;
	}
	
	public void printInfoMessage (Output output) {
		output.print(getConsole().getPlainColor(),
				jEdit.getProperty(MI_SHELL_INFO_MSG_PROP));
	}
	
	public void printPrompt(Console console, Output output)
	{
		// No prompt - this is the stdin of the debugged program
	}
	
	public void append(String s) {
		if (s.endsWith("\n"))
			s = s.substring(0, s.length() - 1);
		print(s);
	}
	public void appendError(String s) {
		if (s.endsWith("\n"))
			s = s.substring(0, s.length() - 1);
		printError(s);
	}
	@Override
	public void execute(Console console, String input,
			Output output, Output error, String command) {
		try {
			getWriter().append(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
