/*
 * XSDNGCompletionTest.java
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2011 Eric Le Lay
 *
 * The XML plugin is licensed under the GNU General Public License, with
 * the following exception:
 *
 * "Permission is granted to link this code with software released under
 * the Apache license version 1.1, for example used by the Xerces XML
 * parser package."
 */
package xml.parser;

// {{{ jUnit imports 
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.gjt.sp.jedit.testframework.TestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xml.completion.CompletionInfo;
import xml.completion.ElementDecl;
// }}}
/**
 * $Id$
 */
public class XSDSchemaToCompletionTest{
	private static File testData;

    @BeforeClass
    public static void setUpjEdit() throws IOException{
        TestUtils.beforeClass();
        testData = new File(System.getProperty("test_data")).getCanonicalFile();
        assertTrue(testData.exists());
    }

    @AfterClass
    public static void tearDownjEdit() {
        TestUtils.afterClass();
    }

    @Test
    public void testNant() throws IOException, org.xml.sax.SAXException {
    	File f = new File(testData,"nant/mini-nant.xsd");
    	Map<String,CompletionInfo> schemas = 
    		XSDSchemaToCompletion.getCompletionInfoFromSchema(f.getPath()
    				, null
    				, null
    				, null
    				, TestUtils.view().getBuffer());
		assertEquals(1,schemas.size());
		assertTrue(schemas.containsKey("http://nant.sf.net/release/0.85/nant.xsd"));
		CompletionInfo info = schemas.get("http://nant.sf.net/release/0.85/nant.xsd");
		assertEquals(1,info.elementHash.size()); // only project is top level
		assertThat(info.elementHash.keySet()).containsOnly("project");
		ElementDecl prj = info.elementHash.get("project");
		assertThat(prj.content).containsOnly("if");
		ElementDecl ifd = prj.elementHash.get("if");
		assertSame(ifd, ifd.elementHash.get("if"));
    }
    

}
