/*
 * ZipVFS.java
 * Copyright (c) 2000, 2001, 2002 Andre Kaplan
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


package archive;

import java.io.IOException;
import java.io.InputStream;

import org.gjt.sp.jedit.io.VFS;


public class ZipVFS extends ArchiveVFS {
    public static final String PROTOCOL = "zip";


    public ZipVFS() {
        super(ZipVFS.PROTOCOL);
    }


    public int getCapabilities() {
        return VFS.READ_CAP;
    }


    protected InputStream openArchiveStream(InputStream in) throws IOException {
        return ArchiveUtilities.openZipStream(in);
    }
}

