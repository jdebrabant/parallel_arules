/*
  JarClassLoader.java

  (P)2000-2001 Laurentiu Cristofor
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

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;

/**
 
   This class implements a simple class loader 
   that can be used to load at runtime 
   classes contained in a JAR file.
 
   @version 1.0
   @author Laurentiu Cristofor
 
*/
public class JarClassLoader extends ClassLoader
{
  private Hashtable jarContents;

  /**
   * Creates a new JarClassLoader that will allow the loading
   * of classes stored in a jar file.
   *
   * @param jarFileName   the name of the jar file
   * @exception IOException   an error happened while reading
   * the contents of the jar file
   */
  public JarClassLoader(String jarFileName)
    throws IOException
  {
    // first get sizes of all files in the jar file
    Hashtable sizes = new Hashtable();

    ZipFile zf = new ZipFile(jarFileName);

    Enumeration e = zf.entries();
    while (e.hasMoreElements())
      {
	ZipEntry ze = (ZipEntry)e.nextElement();
	// make sure we have only slashes, we prefer unix, not windows
	sizes.put(ze.getName().replace('\\', '/'), 
		  new Integer((int)ze.getSize()));
      }

    zf.close();

    // second get contents of the jar file and place each
    // entry in a hashtable for later use
    jarContents = new Hashtable();

    JarInputStream jis = 
      new JarInputStream
	(new BufferedInputStream
	  (new FileInputStream(jarFileName)));

    JarEntry je;
    while ((je = jis.getNextJarEntry()) != null)
      {
	// make sure we have only slashes, we prefer unix, not windows
	String name = je.getName().replace('\\', '/');

	//System.err.println("JarClassLoader debug: loading " + name);

	// get entry size from the entry or from our hashtable
	int size;
	if ((size = (int)je.getSize()) < 0)
	  size = ((Integer)sizes.get(name)).intValue();

	// read the entry
	byte[] ba = new byte[size];
	int bytes_read = 0;
	while (bytes_read != size)
	  {
	    int r = jis.read(ba, bytes_read, size - bytes_read);
 	    if (r < 0)
	      break;
	    bytes_read += r;
	  }
	if (bytes_read != size)
	  throw new IOException("cannot read entry");
	
	jarContents.put(name, ba);
      }

    jis.close();
  }

  /**
   * Looks among the contents of the jar file (cached in memory)
   * and tries to find and define a class, given its name.
   *
   * @param className   the name of the class
   * @return   a Class object representing our class
   * @exception ClassNotFoundException   the jar file did not contain
   * a class named <code>className</code>
   */
  public Class findClass(String className)
    throws ClassNotFoundException
  {
    //System.err.println("JarClassLoader debug: finding " + className);

    // transform the name to a path
    String classPath = className.replace('.', '/') + ".class";

    byte[] classBytes = (byte[])jarContents.get(classPath);

    if (classBytes == null)
      throw new ClassNotFoundException();

    return defineClass(className, classBytes, 0, classBytes.length);
  }
}
