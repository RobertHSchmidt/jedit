package superabbrevs;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

import superabbrevs.model.Abbreviation;
import superabbrevs.model.Mode;

/**
 * @author sune
 * Created on 28. januar 2007, 00:35
 *
 */
public class AbbrevsOptionPaneController {
    Hashtable<String,Mode> modes = new Hashtable<String, Mode>();
    
    private final Persistence persistence;
	private final ModeService modeService;

	public ModeService getModeService() {
		return modeService;
	}

	public AbbrevsOptionPaneController(ModeService modeService, Persistence persistence) {
        this.modeService = modeService;
        this.persistence = persistence;
    }
    
    public Set<Abbreviation> loadsAbbrevs(String modeName) {
        return loadMode(modeName).getAbbreviations();
    }
    
    public Mode loadMode(String modeName) {
    	if (modes.containsKey(modeName)) {
            return modes.get(modeName);
        } else {
            Mode mode = persistence.loadMode(modeName);
            modes.put(modeName, mode);
            return mode;
        }
    }

    public void saveAbbrevs() throws IOException {
        for(Mode mode : modes.values()) {
            persistence.saveMode(mode);  
        }
    }
}
