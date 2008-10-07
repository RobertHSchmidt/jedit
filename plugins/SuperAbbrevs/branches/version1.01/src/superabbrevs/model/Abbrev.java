package superabbrevs.model;

/**
 * @author sune
 * Created on 27. januar 2007, 21:58
 *
 */
public class Abbrev implements Comparable<Abbrev> {
        
    public enum ReplacementTypes {
        AT_CARET ("At caret"),
        BUFFER ("Buffer"),
        LINE ("Line"),
        WORD ("Word"),
        CHAR ("Character");
        
        private String label;
        ReplacementTypes(String label) {
            this.label = label;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    public enum ReplementSelectionTypes {
        NOTHING ("Nothing"),
        SELECTION ("Selection"),
        SELECTED_LINES ("Selected lines");
        
        private String label;
        ReplementSelectionTypes(String label) {
            this.label = label;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    /**
     * A short name of the abbreviation.
     */
    private String name;
    
    /**
     * The abbreviation that will get expanded.
     */
    private String abbreviation;    
    
    /**
     * The expansion of the abbreviation.
     */
    private String expansion;
    
    /**
     * True if the abbreviation should be indented when changed.
     */
    public boolean autoIndent;

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public boolean isAutoIndent() {
        return autoIndent;
    }

    public void setAutoIndent(boolean autoIndent) {
        this.autoIndent = autoIndent;
    }

    public String getExpansion() {
        return expansion;
    }

    public void setExpansion(String expansion) {
        this.expansion = expansion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WhenInvokedAsCommand getWhenInvokedAsCommand() {
        return whenInvokedAsCommand;
    }

    public void setWhenInvokedAsCommand(WhenInvokedAsCommand whenInvokedAsCommand) {
        this.whenInvokedAsCommand = whenInvokedAsCommand;
    }
        
    public static class WhenInvokedAsCommand {
        /**
         * The type of input provided for the abbreviation generation.
         */
        public ReplacementTypes replacementType = ReplacementTypes.AT_CARET;
    
        /**
         * The type of input provided for the abbreviation generation when text 
         * is selected in the buffer.
         */
        public ReplementSelectionTypes replacementSelectionType = 
                ReplementSelectionTypes.NOTHING;
    }
    
    public WhenInvokedAsCommand whenInvokedAsCommand = new WhenInvokedAsCommand();
    
    /** 
     * Creates a new instance of Abbrev 
     */
    public Abbrev(String name, String abbreviation, String expansion) {
        this.name = name; 
        this.abbreviation = abbreviation;
        this.expansion = expansion;
    }
    
    @Override
    public String toString() { 
        return name + " (" + abbreviation + ")";
    }

    public int compareTo(Abbrev o) {
        return name.compareToIgnoreCase(((Abbrev)o).name);
    }
}
