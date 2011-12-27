/*
  DBCacheWriter.java

  (P)1999-2001 Laurentiu Cristofor
*/

/*

laur.dm.ar - A Java package for association rule mining 
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


The laur.dm.ar package was written by Laurentiu Cristofor (laur@cs.umb.edu).

*/

package laur.dm.ar;

import java.io.*;

/**

   A DBCacheWriter serializes itemsets to a cache.
   
   @version 1.0
   @author Laurentiu Cristofor

*/
public class DBCacheWriter
{
  private ObjectOutputStream outstream;
  private String filename;

  /**
   * Initializes a DBCacheWriter to write to the specified cache file.
   *
   * @param name   name of the cache file
   * @exception IllegalArgumentException   <code>name</code> is null
   * @exception IOException   from java.io package
   */                                
  public DBCacheWriter(String name)
    throws IOException 
  {
    if (name == null)
      throw new IllegalArgumentException("Constructor argument must be non null");

    filename = name; 
    outstream = new ObjectOutputStream(new FileOutputStream(filename));
  }

  /**
   * Closes the cache file.
   *
   * @exception IOException   from java.io package
   */                                
  public void close()
    throws IOException
  {
    outstream.close();
  }

  /**
   * Write an itemset to the cache.
   *
   * @exception IOException   from java.io package
   */                
  public void writeItemset(Itemset is)
    throws IOException
  {
    outstream.writeObject(is);
  }

  /**
   * sample usage and testing
   */
  public static void main(String args[])
  {
    try
      {
	DBCacheWriter dbcache = new DBCacheWriter("test.cache");
	
	Itemset is = new Itemset();

	is.add(1);
	is.add(13);
	is.setSupport(3.2);
	dbcache.writeItemset(is);
	
	is = new Itemset();
	is.add(2);
	is.add(7);
	is.setSupport(44.72);
	dbcache.writeItemset(is);
	
	is = new Itemset();
	is.add(10);
	is.add(5);
	is.add(5);
	is.add(7);
	is.setSupport(13.2);
	dbcache.writeItemset(is);
	
	is = new Itemset();
	is.add(51);
	is.add(13);
	is.setSupport(3.33);
	dbcache.writeItemset(is);

	dbcache.close();
      }
    catch (IOException e)
      {
	System.out.println(e);
      }
  }
}
 
