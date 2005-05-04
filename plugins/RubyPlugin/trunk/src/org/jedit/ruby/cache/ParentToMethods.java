/*
 * ParentToMethods.java - 
 *
 * Copyright 2005 Robert McKinnon
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
package org.jedit.ruby.cache;

import org.jedit.ruby.ast.Method;
import org.jedit.ruby.ast.ParentMember;

import java.util.*;

/**
 * @author robmckinnon at users.sourceforge.net
 */
class ParentToMethods {

    private Map<String, Set<Method>> fullNameToMethods = new HashMap<String, Set<Method>>();
    private Map<String, Set<Method>> nameToMethods = new HashMap<String, Set<Method>>();
    private NameToParents nameToParents;
    private List<Method> allMethods;

    void setNameToParents(NameToParents nameToParents) {
        this.nameToParents = nameToParents;
    }

    List<Method> getMethodList(String memberName) {
        Set<Method> methodSet = getMethodSet(memberName);
        List<Method> methods = new ArrayList<Method>(methodSet);

        if (methods.size() > 0) {
            Collections.sort(methods);
        }

        return methods;
    }

    Set<Method> getMethodSet(String memberName) {
        Set<Method> methodSet = fullNameToMethods.get(memberName);
        if (methodSet == null) {
            methodSet = nameToMethods.get(memberName);
        }
        if (methodSet == null) {
            methodSet = new HashSet<Method>();
        }
        return new HashSet<Method>(methodSet);
    }

    /**
     * Note: Have to add methods separately because there
     * may be some classes defined across more than one file.
     */
    void add(ParentMember member) {
        Set<Method> methods = member.getMethods();
        add(member, methods);
    }

    void add(ParentMember member, Set<Method> methods) {
        String fullName = member.getFullName();
        String name = member.getName();

        load(fullName, methods, name, fullNameToMethods, nameToMethods);
    }

    private void load(String fullName, Set<Method> methods, String name, Map<String, Set<Method>> fullNameToMethods, Map<String, Set<Method>> nameToMethods) {
        if (fullNameToMethods.containsKey(fullName)) {
            fullNameToMethods.get(fullName).addAll(methods);
            nameToMethods.get(name).addAll(methods);
        } else {
            fullNameToMethods.put(fullName, methods);
            nameToMethods.put(name, methods);
        }
    }

    void clear() {
        fullNameToMethods.clear();
        nameToMethods.clear();
    }

    List<Method> getAllMethods() {
        if(allMethods == null) {
            allMethods = new ArrayList<Method>();

            for (String parentName : nameToParents.getAllParentNames()) {
                allMethods.addAll(getMethodList(parentName));
            }
        }

        return allMethods;
    }

    void reset() {
        allMethods = null;
    }
}
