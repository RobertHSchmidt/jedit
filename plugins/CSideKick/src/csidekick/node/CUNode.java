/*
Copyright (c) 2005, Dale Anson
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, 
    this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
    this list of conditions and the following disclaimer in the documentation 
    and/or other materials provided with the distribution.
    * Neither the name of the <ORGANIZATION> nor the names of its contributors 
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
package csidekick.node;
import java.util.*;

// an extension of TigerNode for a compilation unit
public class CUNode extends TigerNode {
    
    private String packageName = "";
    private List includes = null;
    private Results results = null;
    
    public CUNode() {
        super( "", 0 );
    }
    
    public void setPackageName(String name) {
        packageName = name;   
    }
    
    public String getPackageName() {
        return packageName;   
    }

    public int getOrdinal() {
        return 0;
    }
    
    public void addInclude(IncludeNode in) {
        if (in == null)
            return;
        if (includes== null)
        	includes = new ArrayList();
        includes.add(in);
    }
    
    /** @return List<String> */
    public List getIncludes() {
        List list = new ArrayList();
        for (Iterator it = includes.iterator(); it.hasNext(); ) {
            list.add(((IncludeNode)it.next()).getName());   
        }
        Collections.sort(list);
        return list;
    }
    
    /** @return List<ImportNode> */
    public List getIncludeNodes() {
        return new ArrayList(includes);   
    }
    
    public IncludeNode getImport(String name) {
        if (includes == null) {
            return null;   
        }
        for (Iterator it = includes.iterator(); it.hasNext(); ) {
            IncludeNode in = (IncludeNode)it.next();
            if (in.getName().equals(name)) {
                return in;   
            }
        }
        return null;
    }
    
    public void setResults(Results r) {
        results = r;   
    }
    
    public Results getResults() {
        return results;   
    }
    
    public String toString() {
        return super.toString() + (results != null ? ", " + results.toString() : "");   
    }
}


