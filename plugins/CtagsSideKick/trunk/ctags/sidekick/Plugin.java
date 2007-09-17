/*
Copyright (C) 2006  Shlomy Reinstein

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package ctags.sidekick;
import org.gjt.sp.jedit.EditPlugin;


public class Plugin extends EditPlugin {
	public static final String NAME = "CtagsSideKick";
	public static final String OPTION_PREFIX = "options.CtagsSideKick.";
	public static final String CTAGS_MODE_OPTIONS = "options.CtagsSideKick.mode.ctags_options";

	@Override
	public void start() {
		MapperManager.start();
		SorterManager.start();
		super.start();
	}
	
	@Override
	public void stop() {
		MapperManager.stop();
		SorterManager.stop();
		super.stop();
	}

}
