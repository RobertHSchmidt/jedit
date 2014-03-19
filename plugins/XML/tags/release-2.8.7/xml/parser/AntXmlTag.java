
package xml.parser;

import java.util.*;
import javax.swing.Icon;

import javax.swing.text.Position;
import org.xml.sax.Attributes;

import eclipseicons.EclipseIconsPlugin;

public class AntXmlTag extends RenamedXmlTag {

    // TODO: figure out which is the default target and use the target_default.png icon.

    public AntXmlTag(String name, String namespace, Position start, Attributes attributes) {
        super(name, namespace, start, attributes);
        String idName = null;
        String idValue = null;

        Map<String, String> attrs = new LinkedHashMap<String, String>();

        for (int i = 0; i < attributes.getLength(); i++) {
            String aname = attributes.getQName(i);
            String value = attributes.getValue(i);
            attrs.put(aname, value);

            if (attributes.getLocalName(i).equalsIgnoreCase("id") || attributes.getType(i).equals("ID")) {
                idName = aname;
                idValue = value;
            }
        }

        StringBuffer buf = new StringBuffer();
        buf.append(name).append(' ');
        String realName = attrs.get("name");
        if (realName != null) {
            buf.append("name=\"");
            buf.append(realName);
            buf.append("\" ");
            setNewName(realName);
        }
        for (String key : attrs.keySet()) {
            if ("name".equals(key)) {
                continue;
            }
            String value = attrs.get(key);
            buf.append(key);
            buf.append("=\"");
            buf.append(value);
            buf.append("\" ");
        }

        attributeString = buf.toString();

        if (idName == null) {
            idAttributeString = name;
        } else {
            idAttributeString = name + ' ' + idName + "=\"" + idValue + '"';
        }
    }
    
    public Icon getIcon() {
        if ("project".equals(name)) {
            return EclipseIconsPlugin.getIcon("ant.gif"); 
        }
        if ("target".equals(name)) {
            return EclipseIconsPlugin.getIcon("targetpublic_obj.gif");
        }
        if ("property".equals(name)) {
            return EclipseIconsPlugin.getIcon("ant_property.png");   
        }
        if ("import".equals(name)) {
            return EclipseIconsPlugin.getIcon("ant_import.png");
        }
        if ("macrodef".equals(name)) {
            return EclipseIconsPlugin.getIcon("ant_macrodef.png");
        }
        if ("taskdef".equals(name)) {
            return EclipseIconsPlugin.getIcon("ant_taskdef.png");
        }
        return EclipseIconsPlugin.getIcon("ant_task.png");
    }
    
}
