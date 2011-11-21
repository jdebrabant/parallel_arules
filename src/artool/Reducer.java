/*
 Reducer.java
 
 (P)2000-2001 Laurentiu Cristofor
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
import laur.tools.Timer;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This program mines a database using the algorithms selected by the
 * user.
 *
 * @version 	1.0
 * @author	Laurentiu Cristofor
 */
class Reducer
{
	private static String dbName;
	private static String cacheName;
	
	private static double min_support;
	private static double min_confidence;
	
	private static int alg_id;
	private static int alg_rules_id;
	
	private static int quiet;
	
	public static void main(String args[])
	{
		if (args.length != 1)
		{
			System.out.println("usage: java Reducer <db name>");
			System.exit(1); 
		}
		
		dbName = args[0] + ".db";
		cacheName = args[0] + ".cache";
		
		mineDB(dbName);
	}
	
	private static void mineDB(String dbName)
	{
		try
		{
			// Initialize algorithms
			FrequentItemsetsMiner alg = new FPgrowth(); 
			
			/*
			if (alg_id == 0)
				System.out.print("Using existing cache file and ");
			else if (alg_id == 1)
				alg = new Apriori();
			else if (alg_id == 2)
				alg = new Closure();
			else if (alg_id == 3)
				alg = new ClosureOpt();
			else if (alg_id == 4)
				alg = new FPgrowth();
			 
			
			AssociationsMiner alg_rules = null;
			
			if (alg_rules_id == 1)
				alg_rules = new AprioriRules();
			else if (alg_rules_id == 2)
				alg_rules = new CoverRules();
			else if (alg_rules_id == 3)
				alg_rules = new CoverRulesOpt();
			 */
			
			
			ArrayList  columnNames = null;
			long numRows = 0;
			
			Timer timer_fi = null;
			if (alg_id != 0)
			{
				// First step: find frequent itemsets
				DBReader dbr = new DBReader(dbName);
				DBCacheWriter dbcw = new DBCacheWriter(cacheName);
				
				timer_fi = new Timer();
				timer_fi.start();
				alg.findFrequentItemsets(dbr, dbcw, min_support);
				timer_fi.stop();
				
				numRows= dbr.getNumRows();
				
				columnNames = dbr.getColumnNames();
				
				dbr.close();
				dbcw.close();
			}
			
			// Second step: display large itemsets
			// read the contents of the cache in a SET
			DBCacheReader dbcr = new DBCacheReader(cacheName);
			
			SET frequents = new SET();
			SET.initializeSET(frequents, min_support, dbcr);
			dbcr.close();
			
			// get and display the itemsets from the SET (MR)
			
			System.out.println("(" + numRows + ")");
			ArrayList FIs = frequents.getItemsets();

			for (int i = 0; i < FIs.size(); i++) 
			{
				Itemset FI = (Itemset) FIs.get(i);
				for (int j = 0; j < FI.size(); j++) 
				{
					int item = FI.get(j);
					String itemName = (String) columnNames.get(item-1);
					System.out.print(itemName + " ");
				}
				System.out.println("(" + (int) Math.ceil(FI.getSupport() * numRows) + ")");
			}
			
			/*
			 System.out.println("END ITEMSETS");
			 // Third step: find association rules
			 dbcr = new DBCacheReader(cacheName);
			 
			 Timer timer_ar = new Timer();
			 timer_ar.start();
			 ArrayList rules = alg_rules.findAssociations(dbcr, 
			 min_support, 
			 min_confidence);
			 timer_ar.stop();
			 
			 dbcr.close();
			 
			 // display association rules
			 if (quiet == 0)
			 for (int i = 0; i < rules.size(); i++)
			 System.out.println(rules.get(i));
			 */
			
		}
		catch (Exception e)
		{
			System.out.println("ERROR! " + e);
			//e.printStackTrace();
		}
	}
}
