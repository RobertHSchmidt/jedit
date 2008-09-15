/*
* $Revision$
* $Date$
* $Author$
*
* Copyright (C) 2008 Eric Le Lay
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

package cswilly.jeditPlugins.spell;


//{{{ Imports

//{{{ 	Java Classpath
import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
//}}}

//{{{ 	jEdit
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.options.PluginOptions;

//}}}

//{{{	junit
//annotations
import org.junit.*;
//usual classes
    import static org.junit.Assert.*;
//}}}

//{{{	FEST...
import org.fest.swing.fixture.*;
import org.fest.swing.core.*;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.finder.DialogByTitleFinder;
import static org.fest.swing.fixture.TableCell.row;
import static org.fest.swing.core.matcher.JButtonByTextMatcher.withText;
//}}}

import cswilly.spell.ValidationDialog;


///}}}

import static cswilly.jeditPlugins.spell.TestUtils.*;

/**
 * Test the functionality of the Options pane
 *  - resistant to sink/invalid/inexistant executable
 *	- test UI
 */
public class SpellCheckOptionPaneTest
{
	private static String testsDir;
	private static String exePath;
	
	@BeforeClass
	public static void setUpjEdit(){
		testsDir = System.getProperty(ENV_TESTS_DIR);
		assertTrue("Forgot to set env. variable '"+ENV_TESTS_DIR+"'",testsDir!=null);
		exePath = System.getProperty(ENV_ASPELL_EXE);
		assertTrue("Forgot to set env. variable '"+ENV_ASPELL_EXE+"'",exePath!=null);

		TestUtils.setUpjEdit();
	}

	@AfterClass
	public static  void tearDownjEdit(){
		TestUtils.tearDownjEdit();
	}

	@Before
	public void beforeTest(){
		jEdit.getPlugin(SpellCheckPlugin.class.getName()).getPluginJAR().activatePluginIfNecessary();
	}

	@After
	public void afterTest(){
		jEdit.getPlugin(SpellCheckPlugin.class.getName()).getPluginJAR().deactivatePlugin(false);
	}
	
	@Test
	public void testEngines(){
		jEdit.setProperty(SpellCheckPlugin.ENGINE_MANAGER_PROP,"Aspell");
		jEdit.setProperty(AspellEngineManager.ASPELL_EXE_PROP,exePath);
		TestUtils.jeditFrame().menuItemWithPath("Plugins","Plugin Options...").click();
		
		DialogFixture optionsDialog = WindowFinder.findDialog(PluginOptions.class).withTimeout(5000).using(TestUtils.robot());
		
		TestUtils.selectPath(optionsDialog.tree(),new String[]{"Plugins","Spell Check","General"});
		
		//VoxSpell, Aspell, Hunspell
		optionsDialog.comboBox("engines").requireSelection("Aspell");
		assertEquals(3,optionsDialog.comboBox("engines").contents().length);
		
		optionsDialog.comboBox("engines").selectItem("Hunspell");
		
		optionsDialog.button(AbstractButtonTextMatcher.withText(JButton.class,"OK")).click();

		//effective?
		assertEquals("Hunspell",jEdit.getProperty(SpellCheckPlugin.ENGINE_MANAGER_PROP));
	}
	
	@Test
	public void testLanguages(){
		jEdit.setProperty(SpellCheckPlugin.ENGINE_MANAGER_PROP,"Aspell");
		jEdit.setProperty(AspellEngineManager.ASPELL_EXE_PROP,exePath);
		jEdit.setProperty(SpellCheckPlugin.MAIN_LANGUAGE_PROP,"en_GB");
		TestUtils.jeditFrame().menuItemWithPath("Plugins","Plugin Options...").click();
		
		DialogFixture optionsDialog = WindowFinder.findDialog(PluginOptions.class).withTimeout(5000).using(TestUtils.robot());
		
		TestUtils.selectPath(optionsDialog.tree(),new String[]{"Plugins","Spell Check","General"});
		
		try{Thread.sleep(2000);}catch(InterruptedException ie){}
		
		//languages (provided that Aspell has English installed
		optionsDialog.comboBox("languages").requireSelection("en_GB");
		
		optionsDialog.comboBox("engines").selectItem("Hunspell");
		
		optionsDialog.button(AbstractButtonTextMatcher.withText(JButton.class,"OK")).click();

	}
	
	
	@Test
	public void testCheckOnSave()
	{
		jEdit.setProperty(SpellCheckPlugin.ENGINE_MANAGER_PROP,"Aspell");
		TestUtils.jeditFrame().menuItemWithPath("Plugins","Plugin Options...").click();
		
		DialogFixture optionsDialog = WindowFinder.findDialog(PluginOptions.class).withTimeout(5000).using(TestUtils.robot());
		
		TestUtils.selectPath(optionsDialog.tree(),new String[]{"Plugins","Spell Check","General"});
		
		
		//spellcheck on save
		optionsDialog.checkBox("SpellCheckOnSave").requireNotSelected().click();
		

		optionsDialog.button(AbstractButtonTextMatcher.withText(JButton.class,"OK")).click();

		//effective?
		assertTrue(jEdit.getBooleanProperty(SpellCheckPlugin.SPELLCHECK_ON_SAVE_PROP));
		jEdit.setBooleanProperty(SpellCheckPlugin.SPELLCHECK_ON_SAVE_PROP,false);
		TestUtils.jeditFrame().menuItemWithPath("Plugins","Plugin Options...").click();
		optionsDialog = WindowFinder.findDialog(PluginOptions.class).withTimeout(5000).using(TestUtils.robot());
		TestUtils.selectPath(optionsDialog.tree(),new String[]{"Plugins","Spell Check","General"});
		optionsDialog.button(AbstractButtonTextMatcher.withText(JButton.class,"OK")).click();		
	}
	
}
