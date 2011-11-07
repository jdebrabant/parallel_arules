/*
  diffcache.java
 
  (P)2001 Laurentiu Cristofor
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

import laur.dm.ar.*;

import java.util.ArrayList;

/**
 * This program displays the differences between 2 cache files
 *
 * @version 1.0
 * @author Laurentiu Cristofor
 */
public class diffcache
{
  private static void showUsage()
  {
    System.out.println("diffcache v1.0 (C)2002 Laurentiu Cristofor\n\nThis program compares the contents of 2 cache files.\n\nUsage:\n\njava diffcache <cache-file1> <cache-file2>");
    System.exit(0);
  }

  public static void main(String args[])
  {
    try
      {
	if (args.length != 2)
	  showUsage();

	String cache1 = args[0];
	String cache2 = args[1];

	DBCacheReader dbcr1 = new DBCacheReader(cache1);
	SET set1 = new SET();
	SET.initializeSET(set1, 0, dbcr1);
	dbcr1.close();

	DBCacheReader dbcr2 = new DBCacheReader(cache2);
	SET set2 = new SET();
	SET.initializeSET(set2, 0, dbcr2);
	dbcr2.close();

	ArrayList frequents1 = set1.getItemsets();
	System.out.println("There are " + frequents1.size() 
			   + " itemsets in " + cache1);
	ArrayList frequents2 = set2.getItemsets();
	System.out.println("There are " + frequents2.size() 
			   + " itemsets in " + cache2);

	if (frequents1.size() != frequents2.size())
	  {
	    System.out.println("Cache files have different size!");
	    System.exit(0);
	  }

	int num_diff = 0;
	for (int i = 0; i < frequents1.size(); i++)
	  {
	    Itemset is = (Itemset)frequents1.get(i);
	    double support2 = set2.getSupport(is);
	    
	    if (is.getSupport() != support2)
	      {
		num_diff++;
		System.out.print("{");
		int j = 0;
		for ( ; j < is.size() - 1; j++)
		  System.out.print("" + is.get(j) + " ");
		System.out.print("" + is.get(j));
		System.out.println("} " + is.getSupport() 
				   + " <-][-> " + support2);
	      }
	    }

	if (num_diff == 0)
	  System.out.println("No differences encountered.");
	else if (num_diff == 1)
	  System.out.println("" + num_diff + " difference encountered!");
	else
	  System.out.println("" + num_diff + " differences encountered!");
      }
    catch (Exception e)
      {
	System.out.println("ERROR! " + e);
      }
  }
}
