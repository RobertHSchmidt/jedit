package jimporter.searchmethod;

import java.io.File;

/**
 * Defines any search method that traverses a structure to find a class.
 *
 * @author Matthew Flower
 * @since   September 5, 2002
 */
public abstract class TraverseSearchMethod extends SearchMethod {
    /**
     * Constructor that sets a couple of properties and stores this SearchMethod
     * in the list of all available search methods.
     *
     * @param propertyID    a <code>String</code> value which contains the unique
     * identifier for this search method.
     * @param nameProperty  a <code>String</code> value which contains the jEdit
     * property that is used to look up a human-readable name for this search 
     * method.
     */
    public TraverseSearchMethod(String propertyID, String nameProperty) {
        super(propertyID, nameProperty);
    }
    
    /**
     * Character that separates the directories of a jar file. This appears to
     * be a forward slash for both windows and unix.
     */
    protected char jarPathSeparatorChar = '/';
    /**
     * The character used to separate directories in a jar file name.
     */
    protected String jarPathSeparator = "/";

    /**
     * Take an entry in a zip (or jar) file, assume that it is actually a java
     * class, and transform the path to that entry into a java class.
     *
     * @param className  a <code>String</code> value containing the textual
     * version of the path to an entry of a zip file.
     * @return The path of a class in a jar formatted to look like a class with
     * a prepended package name.
     * @see #normalizeFSClassName
     */
    protected String normalizeJarClassName(String className) {
        //First, remove the trailing .class
        className = className.substring(0, className.length() - 6);
        //Now, change slashes to periods
        className = className.replace(jarPathSeparatorChar, '.');

        return className;
    }
    
    /**
     * This method takes a <code>String</code> describing the location of a file
     * in the filesystem and converts it to the fully qualified java version of
     * the class name.
     *
     * @param className a <code>String</code> value containing the location
     * of file in the filesystem.
     * @param locationPrefix  a <code>String</code> value containing the
     * directory that is the root of the filesystem classpath element. By
     * subtracting this directory from the file you find, you get the fully
     * qualified java class name
     * @return a <code>String</code> containing the fully qualified class name.
     * @see #normalizeJarClassName
     */
    protected String normalizeFSClassName(String className, String locationPrefix) {
        className = className.substring(locationPrefix.length() + 1, className.length() - ".class".length());
        className = className.replace(File.separatorChar, '.');

        return className;
    }
}
