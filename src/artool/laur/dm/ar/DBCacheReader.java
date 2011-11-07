/*
  DBCacheReader.java

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

   A DBCacheReader deserializes itemsets from cache.
   
   @version 1.0
   @author Laurentiu Cristofor

*/
public class DBCacheReader
{
  private ObjectInputStream instream ;
  private String filename;

  /**
   * Initializes a DBCacheReader to read from the specified cache file.
   *
   * @param name   name of the cache file
   * @exception IllegalArgumentException   <code>name</code> is null
   * @exception IOException   from java.io package
   */                                
  public DBCacheReader(String name) 
    throws IOException
  {
    if (name == null)
      throw new IllegalArgumentException("Constructor argument must be non null");

    filename = name;
    instream = new ObjectInputStream(new FileInputStream(filename));
  }

  /**
   * Closes the cache file.
   *
   * @exception IOException   from java.io package
   */                                
  public void close()
    throws IOException
  {
    instream.close();
  }

  /**
   * Return the first itemset from cache.
   *
   * @exception IOException   from java.io package
   * @exception ClassNotFoundException   from java.io package
   * @return   first itemset in cache
   */                           
  public Itemset getFirstItemset()
    throws IOException, ClassNotFoundException
  {
    instream.close();
    instream = new ObjectInputStream(new FileInputStream(filename));
    return ((Itemset)instream.readObject());
  }

  /**
   * Return next itemset from cache.
   *
   * @exception EOFException   from java.io package, when end of cache
   * is reached
   * @exception IOException   from java.io package
   * @exception ClassNotFoundException   from java.io package
   * @return   next itemset in cache
   */                           
  public Itemset getNextItemset()
    throws IOException, ClassNotFoundException
  {
    return ((Itemset)instream.readObject());
  }

  /**
   * sample usage and testing
   */
  public static void main(String[] args)
  {
    try
      {
	DBCacheReader dbcache = new DBCacheReader("test.cache");

	try
	  {
	    while (true)
	      System.out.println(dbcache.getNextItemset());
	  }
	catch (EOFException e)
	  {
	    dbcache.close();
	  }
      }
    catch (Exception e)
      {
	System.out.println(e);
      }
  }
}
