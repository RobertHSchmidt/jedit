/*
Copyright (c) 2007, Dale Anson
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.
* Neither the name of the author nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ise.plugin.svn.io;

import java.awt.Color;
import java.io.*;

import java.util.logging.*;

import org.gjt.sp.jedit.View;

import ise.plugin.svn.gui.OutputPanel;

import ise.plugin.svn.SVNPlugin;
import ise.plugin.svn.action.NodeActor;

public class ConsolePrintStream extends PrintStream {

    public static String LS = System.getProperty( "line.separator" );

    public ConsolePrintStream( OutputStream os ) {
        super( os, true );
    }

    public ConsolePrintStream( View view ) {
        super( new LogOutputStream( view ), true );
    }

    // print a message to the system shell in the Console plugin.  This is an
    // easy way to display output without a lot of work.
    public void print( String msg ) {
        print( msg, Level.INFO );
    }

    public void print( String msg, Level level ) {
        if ( msg == null || msg.length() == 0 ) {
            return ;
        }
        if ( level == null ) {
            level = Level.INFO;
        }
        if ( out instanceof LogOutputStream ) {
            LogOutputStream los = ( LogOutputStream ) out;
            los.setLevel( level );
        }
        super.print( msg );
    }

    public void println( String msg ) {
        print( msg + LS );
    }

    public void println( String msg, Level level ) {
        print( msg + LS, level );
    }


    public void printError( String msg ) {
        print( msg + LS, Level.SEVERE );
    }

}
