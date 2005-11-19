package ise.calculator;


import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * An RPN calculator. I googled all over, but didn't find any open source
 * rpn calculators, so wrote this one. It does all the math functions in
 * java.lang.Math, plus the standard add, subtract, multiply, divide, and mod
 * operations. It uses the standard 4 register stack rpn found in HP calculators,
 * and supports double (float), integer, hexidecimal, octal, and
 * binary numbers. 
 * <p>
 * Some of this is overkill, I borrowed the math, num, and op classes from the
 * Math task that I wrote for Ant, which goes to a lot of trouble to handle 
 * complex nested formulas, probably it is much more than is needed for this
 * calculator.
 * @author Dale Anson, July 2003
 */
public class Calculator extends JFrame {
   
   public final static String PREFS_NODE = "/ise/calculator";
   public final static Preferences PREFS = Preferences.userRoot().node(PREFS_NODE);
   
   public Calculator() {
      super( "Calculator" );

      addWindowListener( new WindowAdapter() {
               public void windowClosing( WindowEvent we ) {
                  System.exit( 0 );
               }
            }
                       );

      setContentPane( new CalculatorPanel() );
      pack();
      setVisible( true );
   }

   public static void main ( String[] args ) {
      new Calculator();
   }
}
