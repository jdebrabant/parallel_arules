/*
  INIManager.java

  (P)1998-2001 Laurentiu Cristofor
*/

/*

laur.tools - A Java package containing general purpose classes
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


The laur.tools package was written by Laurentiu Cristofor (laur@cs.umb.edu).

*/

package laur.tools;

import java.util.*;
import java.io.*;

/**

   An INIManager object can be used to read and modify the contents of
   an initialization file. This class is intended to be used with a
   simple format of initialization file consisting of setting-value
   pairs separated by an '=', and each appearing on a separate line.

   @version 1.0
   @author Laurentiu Cristofor

*/
public class INIManager
{
  private HashMap settings; // here we save the key-value pairs
  private String filename;    // here we store the name of the file

  /**
   * This method is loading the contents of an INI file in memory.
   *
   * @exception FileNotFoundException thrown when the initialization
   * file is not found. This is just an indication that the file
   * didn't exist and there is no sense in getting values out of it.
   * The user can continue by setting default values and then saving
   * the file by a call to save().
   */
  public void open(String filename) throws FileNotFoundException
  {
    String line, key;

    settings = new HashMap();
    this.filename = filename;

    BufferedReader in = new BufferedReader(new FileReader(filename));

    try 
      {
	while ((line = in.readLine()) != null)
	  {
	    StringTokenizer st = new StringTokenizer(line, "=");
	    
	    // ignore line if no tokens
	    if (!st.hasMoreTokens())
	      continue;
	    
	    key = st.nextToken().trim();
	    
	    // no value - ignore key
	    if (!st.hasMoreTokens())
	      continue;
	    
	    settings.put(key, st.nextToken().trim());
	  }
      }
    catch (IOException e)
      {
	System.err.println("IOException in INIManager.open(): " + 
			   e.getMessage());
      }
    finally
      {
	try
	  {
	    in.close();
	  }
	catch (IOException e)
	  {
	    // I tried.
	  }
      }
  }

  /**
   * This method is saving the settings into the INI file. We need to
   * call it only if we have changed settings and we want to save the
   * changes.
   *
   * @exception IOException thrown if the initialization file could
   * not be opened for write. This is signaling some serious error.
   */
  public void save() throws IOException
  {
    String key, value;

    BufferedWriter out = new BufferedWriter(new FileWriter(filename, false));

    Iterator keys = settings.keySet().iterator();

    try
      {
	while (keys.hasNext())
	  {
	    key = (String)keys.next();

	    out.write(key, 0, key.length());

	    out.write("=", 0, 1);

	    value = (String)settings.get(key);
	    out.write(value, 0, value.length());

	    out.newLine();
	  }
      }
    catch (IOException e)
      {
	System.err.println("IOException in INIManager.close: " + 
			   e.getMessage());
      }
    finally
      {
	try
	  {
	    out.close();
	  }
	catch (IOException e)
	  {
	    // I tried.
	  }
      }
  }

  /**
   * Obtain the value corresponding to a given key.
   *
   * @param key   the key whose value we want to get.
   * @return the value corresponding to the given key, or null if no
   * such key.
   */
  public String getValue(String key)
  {
    return (String)settings.get(key);
  }

  /**
   * Set a value for a given key.
   *
   * @param key   the key whose value we want to set.
   * @param value   the value that we want to set.
   */
  public void setValue(String key, String value)
  {
    settings.put(key, value);
  }

  /**
   * Returns an Iterator over all keys.
   *
   * @return an Iterator over all keys.
   */
  public Iterator keys()
  {
    return settings.keySet().iterator();
  }

  /**
   * sample usage and testing
   */
  public static void main(String[] args)
  {
    INIManager inim = new INIManager();

    try
      {
	inim.open("test.ini");
      }
    catch (FileNotFoundException e)
      {
      }

    System.out.println("FILES=" + inim.getValue("FILES"));
    inim.setValue("FILES", "20");
    inim.setValue("BUFFERS", "40");
    System.out.println("FILES=" + inim.getValue("FILES"));
    System.out.println("BUFFERS=" + inim.getValue("BUFFERS"));

    Iterator keys = inim.keys();
    while (keys.hasNext())
      System.out.println(keys.next());

    try
      {
	inim.save();
      }
    catch (FileNotFoundException e)
      {
      }
    catch (IOException e)
      {
	System.out.println("ERROR! " + e);
      }

  }
}
