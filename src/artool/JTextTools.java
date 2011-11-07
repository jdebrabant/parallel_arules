/*
  JTextTools.java
 
  (P)2002 Laurentiu Cristofor
*/

/*

ARtool - Association Rules tools
Copyright (C) 2002  Laurentiu Cristofor


This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
USA


ARtool was written by Laurentiu Cristofor (laur@cs.umb.edu).

*/

import javax.swing.*;

/**
 * A number of useful methods for dealing with JTextFields.
 *
 * @version 	1.0
 * @author	Laurentiu Cristofor
 */
public class JTextTools
{
  /**
   * Method for dealing with an invalid value in a JTextField. The
   * method clears the text field and sets the focus on it, after
   * which it throws an IllegalArgumentException.
   *
   * @param jTxt   the JTextField
   * @exception IllegalArgumentException   always thrown
   */
  public static void dealWithInvalidValue(JTextField jTxt)
  {
    jTxt.setText("");
    jTxt.requestFocus();
    throw new IllegalArgumentException();
  }

  /**
   * Method for getting a double value from a JTextField. If the value
   * is not valid then dealWithInvalidValue is called resulting in an
   * IllegalArgumentException being thrown. Otherwise the value read
   * from the text field is returned.
   *
   * @param jTxt   the JTextField
   * @exception IllegalArgumentException   if text field contains an
   * invalid value
   */
  public static double getDouble(JTextField jTxt)
  {
    String s = jTxt.getText();
    double value = 0.0;

    try
      {
	value = Double.parseDouble(s);
      }
    catch (NumberFormatException e)
      {
	dealWithInvalidValue(jTxt);
      }

    return value;
  }

  /**
   * Method for getting an int value from a JTextField. If the value
   * is not valid then dealWithInvalidValue is called resulting in an
   * IllegalArgumentException being thrown. Otherwise the value read
   * from the text field is returned.
   *
   * @param jTxt   the JTextField
   * @exception IllegalArgumentException   if text field contains an
   * invalid value
   */
  public static int getInt(JTextField jTxt)
  {
    String s = jTxt.getText();
    int value = 0;

    try
      {
        value = Integer.parseInt(s);
      }
    catch (NumberFormatException e)
      {
	dealWithInvalidValue(jTxt);
      }

    return value;
  }
}
