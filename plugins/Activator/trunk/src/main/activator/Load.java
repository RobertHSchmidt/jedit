package activator;

import java.awt.event.ActionEvent;

import org.gjt.sp.jedit.jEdit;

import common.gui.actions.CustomAction;

public class Load extends CustomAction
{
	PluginList.Plugin plugin;

	public Load(PluginList.Plugin plug) {
		super(plug.toString());
		plugin = plug;
	}

	public void actionPerformed(ActionEvent e)
	{
		StopWatch sw = new StopWatch();
		sw.start();
		PluginManager.loadPluginJAR(plugin.getFile().toString());
		sw.stop();
		jEdit.getActiveView().getStatus().setMessage( plugin + " " + jEdit.getProperty("activator.loaded_in", "loaded in") + " " + sw);
	}
}
