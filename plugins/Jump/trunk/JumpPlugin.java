/*
 *  Jump plugin for jEdit
 *  Copyright (c) 2003 Pavlikus
 *
 *  :tabSize=4:indentSize=4:
 *  :folding=explicit:collapseFolds=1:
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

    //{{{ imports
    import org.gjt.sp.jedit.*;
    import org.gjt.sp.jedit.gui.*;
    import org.gjt.sp.jedit.msg.*;
    import org.gjt.sp.util.Log;

    import java.util.*;
    import java.io.*;
    import ctags.bg.*;

    import projectviewer.*;
    import projectviewer.vpt.*;
    import projectviewer.event.*;
    //}}}

public class JumpPlugin extends EditPlugin
{

    //{{{ fields
    public final static String NAME = "JumpPlugin";
    public final static String MENU = "JumpPlugin.menu";
    public final static String PROPERTY_PREFIX = "plugin.JumpPlugin.";
    public final static String OPTION_PREFIX = "options.JumpPlugin.";

    public static Jump jump_actions;
    public static ProjectJumpAction pja;
    public static TagsJumpAction tja;
    public static FilesJumpAction fja;
    public static JumpEventListener listener;
    public static boolean isListenerAdded = false;
    private static HashMap projectBuffers;
    private static ProjectBuffer activeProjectBuffer;
    private static Vector buffersForDelete;//}}}

    //{{{ EditPlugin methods

    //{{{ EditPlugin.start
    public void start()
    {
        jump_actions = new Jump();
    }//}}}

    //{{{ EditPlugin.stop
    public void stop()
    {
        projectRenamedWorkaround();
        String s = System.getProperty("file.separator");

        try
        {
            // Delete unneeded .jump files
            File f;
            for (int i = 0; i < buffersForDelete.size(); i++)
            {
                f = new File(System.getProperty("user.home") + s + ".jedit" + s + "jump" + s + (String) buffersForDelete.get(i));
                f.delete();
            }
        }
        catch (Exception ex)
        {
            Log.log(Log.DEBUG, this, "JumpPlugin: failed drop unused tags");
            ex.printStackTrace();
        }
        if (isListenerAdded == false)
        {
            return;
        }

        // Save all ProjectBuffers
        try
        {
            if (projectBuffers.size() > 0)
            {
                ProjectBuffer pb;
                Vector v = new Vector(projectBuffers.values());

                for (int i = 0; i < v.size(); i++)
                {
                    pb = (ProjectBuffer) v.get(i);
                    pb.ctags_bg.saveBuffer(pb.PROJECT_CTBUFFER, pb.PROJECT_TAGS.toString());
                }
            }
        }
        catch (Exception e)
        {
            Log.log(Log.DEBUG, this, "failed to save tags on exit");
            e.printStackTrace();
        }
        dispose();
    }//}}}

    //}}}

    //{{{ ProjectBuffer's methods

    //{{{ ProjectBuffer getActiveProjectBuffer()
    public static ProjectBuffer getActiveProjectBuffer()
    {
        return activeProjectBuffer;
    }//}}}

    //{{{ boolean setActiveProjectBuffer(ProjectBuffer)

    // TODO: UGLY setProject/ addProject conditions
    public static boolean setActiveProjectBuffer(ProjectBuffer buff)
    {
        try
        {
            //System.out.println("JumpPlugin: setActiveProjectBuffer...");
            if (projectBuffers.containsKey(buff.PROJECT_NAME))
            {
                activeProjectBuffer = (ProjectBuffer) projectBuffers.get(buff.PROJECT_NAME);
                projectBuffers.put(buff.PROJECT_NAME, buff);
                //System.out.println("JumpPlugin: setActiveProjectBuffer!");
                return true;
            }
            else
            {
                addProjectBuffer(buff);
                setActiveProjectBuffer(buff);
                return true;
            }
        }
        catch (Exception e)
        {
            System.out.println("JumpPlugin: setActiveProjectBuffer failed");
            return false;
        }
    }//}}}

    //{{{ void addProjectBuffer(ProjectBuffer buff
    // QUESTION: Did we need this method at all?
    /**
     *  Just add this ProjectBuffer into hash
     */
    public static void addProjectBuffer(ProjectBuffer buff)
    {
        System.out.println("JumpPlugin: addProjectBuffer - " + buff);
        if (buff == null)
        {
            return;
        }
        projectBuffers.put(buff.PROJECT_NAME, buff);
    } //}}}

    //{{{ void removeProjectBuffer(ProjectBuffer buff)
    /**
     *  Removes ProjectBuffer from hash, setting setActiveProjectBuffer(null)
     */
    public static void removeProjectBuffer(ProjectBuffer buff)
    {
        projectBuffers.remove(buff);
        setActiveProjectBuffer(null);
    }//}}}

    //{{{ void removeProjectBuffer(String name)
    /**
     *  Removes ProjectBuffer from hash, setting setActiveProjectBuffer(null)
     *  @param name - @see ProjectBuffer.PROJECT_NAME
     */
    public static void removeProjectBuffer(String name)
    {
        // if (activeProjectBuffer == buff) {}
        ProjectBuffer b = (ProjectBuffer) projectBuffers.get(name);
        if (b != null)
        {
            projectBuffers.remove(b);
            setActiveProjectBuffer(null);

            HistoryModel mo = HistoryModel.getModel("jump.tag_history.project."+name);
            mo.clear();
        }
    } //}}}

    //{{{ ProjectBuffer getProjectBuffer(String name)
    /**
     *  Return ProjectBuffer from hash 
     */
    public static ProjectBuffer getProjectBuffer(String name)
    {
        return (ProjectBuffer) projectBuffers.get(name);
    } //}}}

    //{{{ boolean hasProjectBuffer(String name)
    /**
     *  Check is ProjectBuffer already exists in hash
     *  @param name = @see ProjectBuffer.PROJECT_NAME
     */
    public static boolean hasProjectBuffer(String name)
    {
        return projectBuffers.containsKey(name);
    } //}}}

    //{{{ boolean hasProjectBuffer(ProjectBuffer buff)
    /**
    *   Check is ProjectBuffer already exists in hash
    */
    public static boolean hasProjectBuffer(ProjectBuffer buff)
    {
        return projectBuffers.containsValue(buff);
    } //}}}

    //}}}

    //{{{ Misc methods

    //{{{ JumpEventListener getListener()
    public static JumpEventListener getListener()
    {
        return listener;
    } //}}}

    //{{{ boolean reloadTagsOnProject()
    public static boolean reloadTagsOnProject()
    {
        if (!jump_actions.isJumpEnabled())
        {
            return false;
        }
        if (ProjectViewer.getViewer(jEdit.getActiveView()) != null)
        {

            ProjectViewer pv = ProjectViewer.getViewer(jEdit.getActiveView());
            VPTProject pr = PVActions.getCurrentProject(jEdit.getActiveView());
            return (listener.reloadTags(pv, pr));
        }
        return false;
    } //}}}

    //{{{ void init()
    /**
     *  Init all classes here, instead of in start() to avoid long starup time
     */
    public static void init()
    {
        System.out.println("JumpPlugin: init...");
        View v = jEdit.getActiveView();
        pja = new ProjectJumpAction();
        tja = new TagsJumpAction();
        fja = new FilesJumpAction();
        listener = new JumpEventListener();
        projectBuffers = new HashMap();

        if (PVActions.getCurrentProject(v) != null)
        {
            ProjectViewer.getViewer(v).addProjectViewerListener(listener, v);
        }
        isListenerAdded = true;
    } //}}}

    //{{{ projectRenamedWorkaround()
    /**
     *  Since PViewer have't PROJECT_RENAMED event, here I try to determine coincidence of project files and .jump files...
     */
    private void projectRenamedWorkaround()
    {
        //System.getProperty("user.home")+s+".jedit"+s+"projectviewer"+s+"projects"+s+this.PROJECT_NAME+".jump"
        buffersForDelete = new Vector();
        String s = System.getProperty("file.separator");
        File pDir = new File(System.getProperty("user.home") + s + ".jedit" + s + "projectviewer" + s + "projects" + s);
        try
        {
            List files = new Vector();
            String[] _files = pDir.list();
            files = Arrays.asList(_files);
            String tmp;

            for (int i = 0; i < _files.length; i++)
            {
                tmp = _files[i];
                if (tmp.endsWith(".jump"))
                {
                    if (!files.contains(tmp.replaceAll(".jump", ".xml")))
                    {
                        buffersForDelete.add(tmp);
                        //System.out.println("Add to buffersForDelete - " + tmp.replaceAll(".xml", ".jump"));
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception projectRenamedWorkaround - " + e);
        }
    } //}}}

    //{{{ dispose
    public void dispose()
    {
        activeProjectBuffer = null;
        projectBuffers = null;

        View v = jEdit.getActiveView();
        if (PVActions.getCurrentProject(v) != null)
        {
            ProjectViewer.getViewer(v).removeProjectViewerListener(listener, v);
            System.out.println("JumpPlugin - ProjectViewerListener removed");
        }
        listener.dispose();

        jump_actions = null;
        pja = null;
        tja = null;
        fja = null;
        buffersForDelete = null;
        listener = null;
    } //}}}

    //}}}
}
