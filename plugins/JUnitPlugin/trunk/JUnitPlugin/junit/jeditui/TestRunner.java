/*
* TestRunner.java
* Copyright (c) 2001 - 2003 Andre Kaplan
* Copyright (c) 2006 Denis Koryavov
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

package junit.jeditui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Image;

import java.net.URL;

import java.util.Enumeration;

import javax.swing.*;

import junit.framework.*;

import junit.JEditReloadingTestSuiteLoader;
import junit.JUnitPlugin;
import junit.PluginTestCollector;

import junit.runner.*;

import org.gjt.sp.jedit.gui.BeanShellErrorDialog;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.Log;

/**
* A test runner for jEdit.
*/
public class TestRunner extends BaseTestRunner implements TestRunContext {
        private View fView;
        private DefaultListModel failures;
        private Thread runnerThread;
        private TestResult fTestResult;
        private String classPath;
        private JUnitDockable dockable;
        
        
        //{{{ TestRunner
        public TestRunner(View view) {
                fView = view;
                failures = new DefaultListModel();
        } //}}}
        
        //{{{ _createUI method.
        public JPanel _createUI(String position, boolean selected) {
                return dockable = new JUnitDockable(this, position, selected);
        } //}}}
        
        //{{{ getTestResult method.
        public TestResult getTestResult() {
                return fTestResult;
        } //}}}
        
        //{{{ getClassPath method.
        public String getClassPath() {
                if (classPath == null) {
                        classPath = JUnitPlugin.getClassPath();
                }
                return classPath;
        } 
        //}}}
        
        //{{{ setClassPath method.
        public void setClassPath(String aClassPath) {
                classPath = aClassPath;
        } 
        //}}}
        
        //{{{ createTestCollector method.
        public TestCollector createTestCollector() {
                return new PluginTestCollector(getClassPath());
        } //}}}
        
        //{{{ getView method.
        public View getView() {
                return fView;
        } //}}}
        
        //{{{ runSuite method.
        synchronized public void runSuite() {
                if (runnerThread != null) {
                        fTestResult.stop();
                } else {
                        reset();
                        dockable.showInfo("Loading Test Case...");
                        
                        final String suiteName = dockable.getCurrentTest();
                        final Test testSuite = getTest(suiteName);
                        
                        if (testSuite != null) {
                                doRunTest(testSuite);
                        }
                }
        } 
        //}}}
        
        // {{{ TestRunContext Methods
        /**
        * Run the current test.
        */
        public void runSelectedTest(Test test) {
                rerunTest(test);
        }
        
        public void handleTestSelected(Test test) {
                dockable.showFailureDetail(test);
        }
        
        public ListModel getFailures() {
                return failures;
        }
        // }}}
        
        // {{{ TestRunListener Methods
        
        public void testFailed(final int status, final Test test, final Throwable t) {
                SwingUtilities.invokeLater(
                        new Runnable() {
                                public void run() {
                                        switch (status) {
                                        case TestRunListener.STATUS_ERROR:
                                                dockable.setErrorCount(fTestResult.errorCount());
                                                appendFailure("Error", test, t);
                                                break;
                                        case TestRunListener.STATUS_FAILURE:
                                                dockable.setFailureCount(fTestResult.failureCount());
                                                appendFailure("Failure", test, t);
                                                break;
                                        }
                                }
                        });
        }
        
        public void testStarted(String testName) {
                dockable.showInfo("Running: " + testName);
        }
        
        public void testEnded(String testName) {
                synchUI();
                SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                        if (fTestResult != null) {
                                                dockable.setRunCount(fTestResult.runCount());
                                                dockable.updateProgress(fTestResult.wasSuccessful());
                                        }
                                }
                });
        }
        // }}}
        
        // {{{ BaseTestRunner Methods
        public TestSuiteLoader getLoader() {
                if (getClassPath().length() == 0) {
                        return new JEditReloadingTestSuiteLoader();
                } else {
                        return new JEditReloadingTestSuiteLoader(getClassPath());
                }
        }
        
        protected void clearStatus() {
                dockable.clearStatus();
        }
        
        protected void runFailed(String message) {
                dockable.showStatus(message);
                runnerThread = null;
        }
        // }}}
        
        //{{{ getIconResource method.
        static Icon getIconResource(Class c, String name) {
                URL url = c.getResource(name);
                if (url == null) {
                        Log.log(Log.ERROR, TestRunner.class, "Warning: could not load \"" + name
                                + "\" icon");
                        return null;
                }
                return new ImageIcon(url);
        } 
        //}}}
        
        //{{{ createTestResult method.
        protected TestResult createTestResult() {
                return new TestResult();
        } //}}}
        
        //{{{ rerunTest method.
        private void rerunTest(Test test) {
                if (test instanceof TestSuite) {
                        TestSuite rerunTest = (TestSuite) test;
                        reset();
                        doRunTest(getTest(rerunTest.getName()));
                        return;
                }
                
                if (!(test instanceof TestCase)) {
                        dockable.showInfo("Could not reload " + test.toString());
                        return;
                }
                
                Test reloadedTest = null;
                TestCase rerunTest = (TestCase) test;
                try {
                        Class reloadedTestClass = getLoader().reload(test.getClass());
                        reloadedTest = TestSuite.createTest(reloadedTestClass, rerunTest
                                .getName());
                } catch (Exception e) {
                        new BeanShellErrorDialog(fView, e);
                        dockable.showInfo("Could not reload " + test.toString());
                        return;
                }
                
                TestResult result = createTestResult();
                reloadedTest.run(result);
                String message = reloadedTest.toString();
                
                if (result.wasSuccessful()) {
                        dockable.showInfo(message + " was successful");
                        removeFailure(rerunTest);
                } else if (result.errorCount() == 1) {
                        Enumeration e = result.errors();
                        TestFailure tf = (TestFailure)e.nextElement();
                        appendFailure("Error", test, tf.thrownException());
                        dockable.showStatus(message + " had an error");
                } else {
                        Enumeration e = result.failures();
                        TestFailure tf = (TestFailure)e.nextElement();
                        appendFailure("Failure", test, tf.thrownException());
                        dockable.showStatus(message + " had a failure");
                }
                dockable.repaintViews(reloadedTest, result);
        }
        //}}}
        
        //{{{ doRunTest method.
        private void doRunTest(final Test testSuite) {
                runnerThread = new Thread("TestRunner-Thread") {
                        public void run() {
                                dockable.startTesting(testSuite.countTestCases());
                                long startTime = System.currentTimeMillis();
                                testSuite.run(fTestResult);
                                if (fTestResult.shouldStop()) {
                                        dockable.showStatus("Stopped");
                                } else {
                                        long endTime = System.currentTimeMillis();
                                        long runTime = endTime - startTime;
                                        dockable.showInfo("Finished: " 
                                                + elapsedTimeAsString(runTime)
                                                + " seconds");
                                }
                                dockable.runFinished(testSuite);
                                runnerThread = null;
                                System.gc();
                        }
                };
                
                // make sure that the test result is created before we start the
                // test runner thread so that listeners can register for it.
                
                fTestResult = createTestResult();
                fTestResult.addListener(TestRunner.this);
                dockable.aboutToStart(testSuite);
                runnerThread.start();
        }
        //}}}
        
        //{{{ synchUI method.
        /**
        * Wait until all the events are processed in the event thread
        */
        private void synchUI() {
                try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                                        public void run() {
                                        }
                        });
                } catch (Exception e) {
                        e.printStackTrace();
                }
        } 
        //}}}
        
        //{{{ reset method.
        private void reset() {
                failures.clear();
                dockable.reset();
        } 
        //}}}
        
        //{{{ appendFailure method.
        private void appendFailure(String kind, Test test, Throwable t) {
                failures.addElement(new TestFailure(test, t));
                if (failures.size() == 1)
                        dockable.revealFailure(test);
        } 
        //}}}
        
        //{{{ removeFailure method.
        private void removeFailure(Test test) {
                for(int i = 0; i < failures.getSize(); i++) {
                        TestFailure tf = (TestFailure)failures.getElementAt(i);
                        if(tf.failedTest() == test) {
                                failures.removeElementAt(i);
                                return; 
                        }
                }
        } //}}}
        
        // :collapseFolds=1:tabSize=8:indentSize=8:folding=explicit:
}
