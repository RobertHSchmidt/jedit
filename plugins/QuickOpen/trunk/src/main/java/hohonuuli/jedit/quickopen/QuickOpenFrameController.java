/*
 * @(#)QuickOpenFrameController.java   2011.01.15 at 11:08:08 PST
 *
 * Copyright 2011 Brian Schlining
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package hohonuuli.jedit.quickopen;

import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * @author Brian Schlining
 * @since 2011-01-05
 */
public class QuickOpenFrameController {

    private final QuickOpenFrame quickOpenFrame;

    /**
     * Constructs ...
     *
     * @param quickOpenFrame
     */
    public QuickOpenFrameController(QuickOpenFrame quickOpenFrame) {
        this.quickOpenFrame = quickOpenFrame;
    }

    /**
     * @return
     */
    public File getDirectory() {
        VFSBrowser vfsBrowser = getVFSBrowser();
        File directory = null;
        if (vfsBrowser != null) {

            String path = vfsBrowser.getDirectory();
            try {
                directory = new File(path);
            }
            catch (Exception e) {
                Log.log(Log.MESSAGE, this, "Failed to parse " + path, e);
            }
        }

        if (directory == null) {
            Log.log(Log.DEBUG, this, "No directory was selected in VFSBrowser. Using directory of current buffer");
            try {
                String path = quickOpenFrame.getView().getBuffer().getDirectory();
                directory = new File(path);
            }
            catch (Exception e) {
                /*
                 We end up here when : 
                   1) jEdit is first started and no clicks in the VFSBrowser have been made AND
                   2) No files are opened AND
                   3) User tries to use quickopen
                 */
                // TODO Grab whatever the default directory used by jEdit is and set it here.
                Log.log(Log.MESSAGE, this, "Failed to parse directory of current buffer", e);
            }

        }

        return directory;
    }

    /**
     * @return
     */
    public VFSBrowser getVFSBrowser() {
        DockableWindowManager windowManager = quickOpenFrame.getView().getDockableWindowManager();
        return (VFSBrowser) windowManager.getDockable("vfs.browser");
    }

    /**
     *
     * @param file
     */
    public void openFile(File file) {
        try {
            jEdit.openFile(quickOpenFrame.getView(), file.getCanonicalPath());
            quickOpenFrame.setVisible(false);
        }
        catch (IOException e) {
            Log.log(Log.MESSAGE, this, "Failed to open " + file, e);
        }
    }
}
