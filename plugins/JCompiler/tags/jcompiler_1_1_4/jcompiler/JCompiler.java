/*
 * JCompiler.java - a wrapper around sun.tools.javac.Main
 * (c) 1999, 2000 - Kevin A. Burton and Aziz Sharif
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

package jcompiler;

import java.lang.reflect.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.*;
import javax.swing.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.util.*;
import buildtools.*;


/**
 * The class that performs the javac compile run.
 */
public class JCompiler {

	/** True, if JDK version is less than 1.2. */
	private final static boolean isOldJDK = (MiscUtilities.compareVersions(System.getProperty("java.version"), "1.2") < 0);

	/** Holds the javac compiler class. */
	private static Class compilerClass = null;

	/** Compiler output is sent to this pipe. */
	private PipedOutputStream pipe = null;


	public JCompiler() {
		pipe = new PipedOutputStream();
	}


	/**
	 * compile a file with sun.tools.javac.Main.
	 *
	 * @param view        the view, where error dialogs should go
	 * @param buf         the buffer containing the file to be compiled
	 * @param pkgCompile  if true, JCompiler tries to locate the base directory
	 *                    of the package of the current file and compiles
	 *                    every outdated file.
	 * @param rebuild     if true, JCompiler compiles <i>every</i> file in the
	 *                    package hierarchy.
	 */
	public void compile(View view, Buffer buf, boolean pkgCompile, boolean rebuild) {
		// Search for the compiler method
		compilerClass = getCompilerClass();
		if (compilerClass == null)
			return;

		// Check output directory:
		String outDirPath = null;
		if (jEdit.getBooleanProperty( "jcompiler.specifyoutputdirectory")) {
			outDirPath = jEdit.getProperty("jcompiler.outputdirectory");
			outDirPath = expandPath(outDirPath);
			File outDir = new File(outDirPath);
			try {
				// canonize outDirPath:
				outDirPath = outDir.getCanonicalPath();
			}
			catch (IOException ioex) {
				sendMessage("jcompiler.msg.errorOutputDir", new Object[] { outDirPath, ioex });
				return;
			}
			if (outDir.exists()) {
				if (!outDir.isDirectory()) {
					sendMessage("jcompiler.msg.noOutputDir", new Object[] {outDirPath });
					return;
				}
			} else {
				int reply = JOptionPane.showConfirmDialog(view,
					jEdit.getProperty("jcompiler.msg.createOutputDir.message", new Object[] {outDirPath }),
					jEdit.getProperty("jcompiler.msg.createOutputDir.title"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
				if (reply != JOptionPane.YES_OPTION) {
					return;
				}
				if (!outDir.mkdirs()) {
					GUIUtilities.message(view, "jcompiler.msg.errorCreateOutputDir", null);
					return;
				}
			}
		}

		// Check for auto save / auto save all
		saveBuffers(view, buf, pkgCompile);

		// Get files to compile:
		String filename = buf.getPath();
		File ff = new File(filename);
		String parent = ff.getParent();
		String[] files = null;
		if (parent != null && pkgCompile == true) {
			// compile/rebuild package: try to get base directory of file
			String sourcedir;
			try {
				sourcedir = JavaUtils.getBaseDirectory(ff.getAbsolutePath());
			}
			catch (IOException ioex) {
				Log.log(Log.ERROR, "JCompiler",
					"couldn't get base directory of file " + filename + ": " + ioex.toString());
				sourcedir = parent;
			}
			FileChangeMonitor monitor = new FileChangeMonitor(
				sourcedir, "java", outDirPath, "class");
			if (rebuild) {
				files = monitor.getAllFiles();
			} else {
				files = monitor.getChangedFiles();
			}
			if (files.length == 0) {
				sendMessage("jcompiler.msg.nofiles", new Object[] { sourcedir });
				return;
			}
			sendMessage("jcompiler.msg.compilefiles", new Object[] {
				new Integer(files.length),
				sourcedir,
				new Integer(outDirPath == null ? 0 : 1),
				outDirPath
			});
		} else {
			sendMessage("jcompiler.msg.compilefile", new Object[] {
				filename,
				new Integer(outDirPath == null ? 0 : 1),
				outDirPath
			});
			files = new String[] { filename };
		}

		// Source path setting:
		String srcPath = expandPath(jEdit.getProperty("jcompiler.sourcepath"));

		// Class path setting:
		String cp = expandPath(jEdit.getProperty("jcompiler.classpath"));

		// Check if package dir should be added to classpath:
		if (jEdit.getBooleanProperty("jcompiler.addpkg2cp")) {
			try {
				String pkgName = JavaUtils.getPackageName(filename);
				Log.log(Log.DEBUG, this, "parent=" + parent + " pkgName=" + pkgName);
				// If no package stmt found then pkgName would be null
				if (parent != null) {
					if (pkgName == null) {
						cp = cp + (cp.length() > 0 ? File.pathSeparator : "") + parent;
					} else {
						String pkgPath = pkgName.replace('.', File.separatorChar);
						Log.log(Log.DEBUG, this, "pkgPath=" + pkgPath);
						if (parent.endsWith(pkgPath)) {
							parent = parent.substring(0, parent.length() - pkgPath.length() - 1);
							cp = cp + (cp.length() > 0 ? File.pathSeparator : "") + parent;
						}
					}
				}
			}
			catch (Exception exp) {
				exp.printStackTrace();
			}
		}

		// Required library path setting:
		String libPath = expandPath(jEdit.getProperty("jcompiler.libpath"));
		cp = cp + (cp.length() > 0 ? File.pathSeparator : "") + expandLibPath(libPath);

		// Construct arguments for javac:
		String[] arguments = constructArguments(cp, srcPath, outDirPath, files);

		// Show command line:
		if (jEdit.getBooleanProperty("jcompiler.showcommandline", false)) {
			StringBuffer msg = new StringBuffer();
			for (int i = 0; i < arguments.length; ++i) {
				msg.append(' ');
				msg.append(arguments[i]);
			}
			sendMessage("jcompiler.msg.showcommandline", new Object[] {
				jEdit.getProperty("jcompiler.compiler.class"),
				jEdit.getProperty("jcompiler.compiler.method"),
				msg.toString()
			});
		}

		// Start the javac compiler...
		boolean ok = invokeCompiler(pipe, arguments);
		System.gc();
		sendMessage("jcompiler.msg.done");

		try { pipe.flush(); }
		catch (IOException ioex) { /* ignore */ }

		return;
	}


	public PipedOutputStream getOutputPipe() {
		return pipe;
	}


	private void sendMessage(String property) {
		sendString(jEdit.getProperty(property));
	}


	private void sendMessage(String property, Object[] args) {
		sendString(jEdit.getProperty(property, args));
	}


	private void sendString(String msg) {
		Log.log(Log.DEBUG, this, msg);
		byte[] bytes = msg.getBytes();

		if (pipe != null && bytes != null) {
			try {
				pipe.write(bytes, 0, bytes.length);
				pipe.flush();
			}
			catch (IOException ioex) {
				// ignored
			}
			catch (NullPointerException ioex) {
				// this exception occurs sometimes on crappy VM implementations
				// like IBM JDK 1.1.8: pipe.write() throws it, if there's
				// no sink, maybe because the connection to the sink has not
				// yet been established or the the thread that creates the
				// sink has stopped.
				Log.log(Log.ERROR, this, "lost the output sink!");
			}
		}
	}


	private Class getCompilerClass() {
		if (compilerClass != null)
			return compilerClass;

		// search for compiler class:
		String className = "sun.tools.javac.Main";
		try {
			compilerClass = Class.forName(className);
		}
		catch (ClassNotFoundException cnf) {
			if (!isOldJDK) {
				// new JDK (>= 1.2): try to find tools.jar:
				String home = System.getProperty("java.home");
				Log.log(Log.DEBUG, JCompiler.class, "java.home=" + home);
				if (home.toLowerCase().endsWith(File.separator + "jre"))
					home = home.substring(0, home.length() - 4);
				File toolsJar = new File(MiscUtilities.constructPath(home, "lib", "tools.jar"));
				if (toolsJar.exists()) {
					try {
						Log.log(Log.DEBUG, JCompiler.class, "loading class " + className + " from " + toolsJar.getCanonicalPath());
						ClassLoader cl = ZipClassLoader.getInstance(toolsJar);
						compilerClass = cl.loadClass(className);
					}
					catch (Exception ex) {
						Log.log(Log.ERROR, JCompiler.class, ex);
						sendMessage("jcompiler.msg.nocompilerclass_jdk12_tools_jar", new Object[] { className, toolsJar, ex.toString() });
						return null;
					}
				} else {
					Log.log(Log.ERROR, JCompiler.class, cnf);
					sendMessage("jcompiler.msg.nocompilerclass_jdk12", new Object[] { className, toolsJar });
					return null;
				}
			} else {
				Log.log(Log.ERROR, JCompiler.class, cnf);
				sendMessage("jcompiler.msg.nocompilerclass_jdk11", new Object[] { className });
				return null;
			}
		}

		return compilerClass;
	}


	private boolean invokeCompiler(OutputStream output, String[] arguments) {
		try {
			// instantiate a new compiler class with the constructor arguments:
			// Main(OutputStream output, String programname = "javac")
			Class clazz = getCompilerClass();
			Constructor constructor = clazz.getConstructor(new Class[] { OutputStream.class, String.class });
			Object compiler = constructor.newInstance(new Object[] { output, "javac" });

			// get the method "compile(String[] args)":
			String[] stringArray = new String[] {};
			Class stringArrayType = stringArray.getClass();
			Method compile = clazz.getMethod("compile", new Class[] { stringArrayType });

			// invoke the method compile(String[] args) on the instance:
			Object returnValue = compile.invoke(compiler, new Object[] { arguments });

			// The returnValue should be a Boolean:
			return ((Boolean)returnValue).booleanValue();
		}
		catch (InvocationTargetException invex) {
			// the invoked method itself has thrown an exception
			Throwable targetException = invex.getTargetException();
			Log.log(Log.ERROR, this, "The compiler method itself just threw a runtime exception: " + targetException.toString());
			targetException.printStackTrace();
			Object[] args = new Object[] { compilerClass, targetException };
			sendMessage("jcompiler.msg.compilermethod_exception", args);
		}
		catch (Exception e) {
			Log.log(Log.ERROR, this, e);
			e.printStackTrace();
			Object[] args = new Object[] { compilerClass, e };
			sendMessage("jcompiler.msg.compilermethod_exception", args);
		}
		return false;
	}


	private String[] constructArguments(String cp, String srcPath, String outDirPath, String[] files) {
		Vector vectorArgs = new Vector();

		if (cp != null && !cp.equals("")) {
			vectorArgs.addElement("-classpath");
			vectorArgs.addElement(cp);
		}

		if (srcPath != null && !srcPath.equals("") && !isOldJDK) {
			vectorArgs.addElement("-sourcepath");
			vectorArgs.addElement(srcPath);
		}

		if (jEdit.getBooleanProperty("jcompiler.genDebug"))
			vectorArgs.addElement("-g");

		if (jEdit.getBooleanProperty("jcompiler.genOptimized"))
			vectorArgs.addElement("-O");

		if (jEdit.getBooleanProperty("jcompiler.showdeprecated"))
			vectorArgs.addElement("-deprecation");

		if (jEdit.getBooleanProperty("jcompiler.specifyoutputdirectory")
			&& outDirPath != null && !outDirPath.equals("")) {
			vectorArgs.addElement("-d");
			vectorArgs.addElement(outDirPath);
		}

		String otherOptions = jEdit.getProperty("jcompiler.otheroptions");
		if (otherOptions != null) {
			StringTokenizer st = new StringTokenizer(otherOptions, " ");
			while (st.hasMoreTokens()) {
				vectorArgs.addElement(st.nextToken());
			}
		}

		for (int i = 0; i < files.length; ++i)
			vectorArgs.addElement(files[i]);

		String[] arguments = new String[vectorArgs.size()];
		vectorArgs.copyInto(arguments);
		return arguments;
	}


	/**
	 * Expand any directory in the path to include all jar or zip files in that directory;
	 *
	 * @param  path  the path to be expanded.
	 * @return the path with directories expanded.
	 */
	private String expandLibPath(String path) {
		StringTokenizer st;
		File f;
		StringBuffer result;
		String token;

		if (path == null || path.length() == 0)
			return "";

		st = new StringTokenizer(path, File.pathSeparator);
		result = new StringBuffer(path.length());

		while (st.hasMoreTokens()) {
			token = st.nextToken();
			f = new File(token);

			if (f.isDirectory())
				result.append(buildPathForDirectory(f));
			else
				result.append(token);

			if (st.hasMoreTokens())
				result.append(File.pathSeparator);
		}

		return result.toString();
	}


	/**
	 * build a path containing the jar and zip files in the directory represented by <code>f</code>.
	 *
	 * @param  f  a directory
	 * @return a classpath containg all the jar and zip files from the given directory.
	 */
	private String buildPathForDirectory(File f) {
		String[] archiveFiles = f.list(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.toLowerCase().endsWith(".jar") || filename.toLowerCase().endsWith(".zip");
			}
		});

		if (archiveFiles.length == 0)
			return "";

		StringBuffer result = new StringBuffer();
		for (int i = 0; i < archiveFiles.length; i++) {
			if (i > 0)
				result.append(File.pathSeparator);
			result.append(f.getPath());
			result.append(File.separator);
			result.append(archiveFiles[i]);
		}

		return result.toString();
	}


	/**
	 * Expand any variables in path.
	 *
	 * NOTE: only looks for $basePath right now.
	 *
	 * @param  path  the path, possibly containing variables
	 * @return the path with all variables expanded.
	 */
	private String expandPath(String path) {
		if (path == null || path.length() == 0)
			return "";

		String basePath = jEdit.getProperty("jcompiler.basepath", "").trim();
		if (basePath.length() == 0)
			return path;

		return replaceAll(path, "$basepath", basePath);
	}


	/**
	 * Returns the string with all occurences of the specified variable replaced
	 * by the specified value.
	 * @param  s  the string
	 * @param  variable  the variable to look for; should begin with "$"
	 * @param  value  to value to be set in for the variable
	 * @return the modified string.
	 */
	private static String replaceAll(String s, String variable, String value) {
		if (s == null || s.length() == 0)
			return s;

		if (variable == null || variable.length() == 0)
			return s;

		if (value == null)
			value = "";

		String result = s;
		int matchIndex = result.indexOf(variable);
		int vlen = variable.length();

		while (matchIndex != -1) {
			result = result.substring(0, matchIndex) + value + result.substring(matchIndex + vlen);
			matchIndex = result.indexOf(variable, matchIndex + vlen);
		}

		return result;
	}


	private void saveBuffers(View view, Buffer current, boolean pkgCompile) {
		String prop = pkgCompile ? "jcompiler.javapkgcompile.autosave" : "jcompiler.javacompile.autosave";
		String which = jEdit.getProperty(prop, "ask");

		if (which.equals("current"))
			saveCurrentBuffer(view, current);
		else if (which.equals("all"))
			saveAllBuffers(view);
		else if (which.equals("ask"))
			saveBuffersAsk(view, current, pkgCompile);
		// do nothing on which == "no"
	}


	/** Save current buffer, if dirty. */
	private void saveCurrentBuffer(View view, Buffer buf) {
		if (buf.isDirty()) {
			buf.save(view, null);
			VFSManager.waitForRequests();
		}
	}


	/** Save all buffers without asking. */
	private void saveAllBuffers(View view) {
		boolean savedSomething = false;
		Buffer[] buffers = jEdit.getBuffers();

		for(int i = 0; i < buffers.length; i++)
			if (buffers[i].isDirty()) {
				buffers[i].save(view, null);
				savedSomething = true;
			}

		if (savedSomething)
			VFSManager.waitForRequests();
	}


	/** Ask for unsaved changes and save. */
	private void saveBuffersAsk(View view, Buffer buf, boolean pkgCompile) {
		boolean savedSomething = false;
		if (pkgCompile) {
			// Check if there are any unsaved buffers:
			Buffer[] buffers = jEdit.getBuffers();
			boolean dirty = false;
			for(int i = 0; i < buffers.length; i++)
				if (buffers[i].isDirty())
					dirty = true;

			if (dirty) {
				int result = JOptionPane.showConfirmDialog(view,
					jEdit.getProperty("jcompiler.msg.saveAllChanges.message"),
					jEdit.getProperty("jcompiler.msg.saveAllChanges.title"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.CANCEL_OPTION)
					return;
				if (result == JOptionPane.YES_OPTION)
					for(int i = 0; i < buffers.length; i++)
						if (buffers[i].isDirty()) {
							buffers[i].save(view, null);
							savedSomething = true;
						}
			}
		} else { // !pkgCompile
			// Check if current buffer is unsaved:
			if (buf.isDirty()) {
				int result = JOptionPane.showConfirmDialog(view,
					jEdit.getProperty("jcompiler.msg.saveChanges.message", new Object[] { buf.getName() }),
					jEdit.getProperty("jcompiler.msg.saveChanges.title"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.CANCEL_OPTION)
					return;
				if (result == JOptionPane.YES_OPTION) {
					buf.save(view, null);
					savedSomething = true;
				}
			}
		}

		if (savedSomething)
			VFSManager.waitForRequests();
	}

}

