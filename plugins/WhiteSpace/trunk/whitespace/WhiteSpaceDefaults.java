/*
 * WhiteSpaceDefaults.java
 * Copyright (c) 2001 Andre Kaplan
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

package whitespace;

import org.gjt.sp.jedit.jEdit;


public class WhiteSpaceDefaults
{
    public static boolean getFoldHighlightDefault() {
       return jEdit.getBooleanProperty(
            "white-space.show-fold-default", true
        );
    }


    public static boolean getFoldTooltipDefault() {
        return jEdit.getBooleanProperty(
            "white-space.show-fold-tooltip-default", true
        );
    }


    public static boolean getBlockHighlightDefault() {
        return jEdit.getBooleanProperty(
            "white-space.show-block-default", false
        );
    }


    public static boolean getSpaceHighlightDefault() {
        return jEdit.getBooleanProperty(
            "white-space.show-space-default", true
        );
    }


    public static boolean getLeadingSpaceHighlightDefault() {
        return jEdit.getBooleanProperty(
            "white-space.show-leading-space-default", true
        );
    }


    public static boolean getInnerSpaceHighlightDefault() {
        return jEdit.getBooleanProperty(
            "white-space.show-inner-space-default", true
        );
    }


    public static boolean getTrailingSpaceHighlightDefault() {
        return jEdit.getBooleanProperty(
            "white-space.show-trailing-space-default", true
        );
    }


    public static boolean getTabHighlightDefault() {
        return jEdit.getBooleanProperty(
            "white-space.show-tab-default", true
        );
    }


    public static boolean getLeadingTabHighlightDefault() {
        return jEdit.getBooleanProperty(
            "white-space.show-leading-tab-default", true
        );
    }


    public static boolean getInnerTabHighlightDefault() {
        return jEdit.getBooleanProperty(
            "white-space.show-inner-tab-default", true
        );
    }


    public static boolean getTrailingTabHighlightDefault() {
        return jEdit.getBooleanProperty(
            "white-space.show-trailing-tab-default", true
        );
    }


    public static boolean getWhitespaceHighlightDefault() {
        return jEdit.getBooleanProperty(
            "white-space.show-whitespace-default", false
        );
    }
}

