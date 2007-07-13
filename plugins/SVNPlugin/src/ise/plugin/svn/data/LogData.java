package ise.plugin.svn.data;

import java.util.Date;
import org.tmatesoft.svn.core.wc.SVNRevision;

public class LogData extends CheckoutData {

    private SVNRevision startRevision = SVNRevision.create(0L);
    private SVNRevision endRevision = SVNRevision.HEAD;
    private int maxLogs = 100;

    public String toString() {
        return "LogData[startRevision=" + startRevision + ", endRevision=" + endRevision + "]";
    }

    public void setMaxLogs(int n) {
        maxLogs = Math.max(0, n);
    }

    public int getMaxLogs() {
        return maxLogs;
    }

    /**
     * Returns the value of startRevision.
     */
    public SVNRevision getStartRevision()
    {
        return startRevision;
    }

    /**
     * Sets the value of startRevision.
     * @param startRevision The value to assign startRevision.
     */
    public void setStartRevision(SVNRevision startRevision)
    {
        this.startRevision = startRevision;
    }

    /**
     * Returns the value of endRevision.
     */
    public SVNRevision getEndRevision()
    {
        return endRevision;
    }

    /**
     * Sets the value of endRevision.
     * @param endRevision The value to assign endRevision.
     */
    public void setEndRevision(SVNRevision endRevision)
    {
        this.endRevision = endRevision;
    }

}
