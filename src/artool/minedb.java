/*
 minedb.java
 
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

import java.io.*; 

/**
 * This program mines a database using the algorithms selected by the
 * user.
 *
 * @version 	1.0
 * @author	Laurentiu Cristofor
 */
class minedb
	{
		private static String dbName;
		private static String cacheName;
		
		private static double min_support;
		private static double min_confidence;
		
		private static int alg_id;
		private static int alg_rules_id;
		
		private static int quiet;
		
		private static void showUsage()
		{
			System.out.println("minedb v1.0 (C)2002 Laurentiu Cristofor\n\nThis program provides a command line interface to several data mining algorithms.\n\nUsage:\n\njava minedb <dbfile> <min_support> <min_confidence> <alg_id> <alg_rules_id> <quiet>\n\nwhere alg_id is one of:\n\t0 for using previously generated cache file\n\t1 for Apriori\n\t2 for Closure\n\t3 for ClosureOpt\n\t4 for FPgrowth\nalg_rules_id is one of:\n\t1 for AprioriRules\n\t2 for CoverRules\n\t3 for CoverRulesOpt\nand quiet is either 1 for minimal output or 0 for verbose output");
			System.exit(0);
		}
		
		public static void main(String args[])
		{
			if (args.length != 6)
				showUsage();
			
			dbName = args[0] + ".db";
			cacheName = args[0] + ".cache";
			
			try
			{
				min_support = Double.parseDouble(args[1]);
				if (min_support < 0 || min_support > 1)
					showUsage();
			}
			catch (NumberFormatException e)
			{
				showUsage();
			}
			
			try
			{
				min_confidence = Double.parseDouble(args[2]);
				if (min_confidence < 0 || min_confidence > 1)
					showUsage();
			}
			catch (NumberFormatException e)
			{
				showUsage();
			}
			
			try
			{
				alg_id = Integer.parseInt(args[3]);
				if (alg_id < 0 || alg_id > 4)
					showUsage();
			}
			catch (NumberFormatException e)
			{
				showUsage();
			}
			
			try
			{
				alg_rules_id = Integer.parseInt(args[4]);
				if (alg_rules_id < 1 || alg_rules_id > 3)
					showUsage();
			}
			catch (NumberFormatException e)
			{
				showUsage();
			}
			
			try
			{
				quiet = Integer.parseInt(args[5]);
				if (quiet < 0 || quiet > 1)
					showUsage();
			}
			catch (NumberFormatException e)
			{
				showUsage();
			}
			
			mineDB(dbName);
		}
		
		private static void mineDB(String dbName)
		{
			try
			{
				long start_time, end_time;
				
				BufferedWriter out = new BufferedWriter(new FileWriter("fim.txt")); 
				
				// Initialize algorithms
				FrequentItemsetsMiner alg = null;
				
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
				 */
				
				alg = new FPgrowth(); // always use the FP-Growth algorithm
				
				AssociationsMiner alg_rules = null;
				
				if (alg_rules_id == 1)
					alg_rules = new AprioriRules();
				else if (alg_rules_id == 2)
					alg_rules = new CoverRules();
				else if (alg_rules_id == 3)
					alg_rules = new CoverRulesOpt();
				
				
				ArrayList  columnNames = null;
				long numRows = 0;
				
				Timer timer_fi = null;
				if (alg_id != 0)
				{
					// First step: find frequent itemsets
					DBReader dbr = new DBReader(dbName);
					DBCacheWriter dbcw = new DBCacheWriter(cacheName);
					
					start_time = System.currentTimeMillis(); 
					timer_fi = new Timer();
					timer_fi.start();
					alg.findFrequentItemsets(dbr, dbcw, min_support);
					timer_fi.stop();
					end_time = System.currentTimeMillis(); 
					
					System.out.println("total run time: " + ((end_time - start_time)/1000)); 
					
					numRows = dbr.getNumRows();
					
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
				
				//System.out.println("(" + numRows + ")");
				ArrayList FIs = frequents.getItemsets();
				if (quiet == 0) {
					for (int i = 0; i < FIs.size(); i++) {
						Itemset FI = (Itemset) FIs.get(i);
						for (int j = 0; j < FI.size(); j++) {
							int item = FI.get(j);
							String itemName = (String) columnNames.get(item-1);
							System.out.print(itemName + " ");
						}
						//System.out.println("(" + (int) Math.ceil(FI.getSupport() * numRows) + ")");
					}
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
