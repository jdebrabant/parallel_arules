//
//  Miner.java
//  
//
//  Created by Justin on 12/11/11.
//  Copyright 2011 Florida State University. All rights reserved.
//

import java.util.*; 
import java.io.*; 
import java.lang.Math;

import laur.dm.ar.*;
import laur.tools.Timer;

public class Miner 
{
	public static void main(String [] args)
	{
		try 
		{			
			if(args.length != 2)
			{
				System.out.println("usage: java <dat input file> <support>"); 
				System.exit(1); 
			}
			
			dat2asc asc_converter = new dat2asc(); 
			asc2db db_converter = new asc2db(); 
							
			try 
			{
				asc_converter.convertToASC(args[0], "temp.asc"); 
				
				// will read temp.asc from disk and write a file in .db format to temp.db
				db_converter.convertToDB("temp.asc", "temp.db"); 
			}
			catch(Exception e)
			{
				System.err.println("Error converting from asc to db"); 
			}
			
			mineDB("temp.db", "temp.cache", .2);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
	
	
	public static void mineDB(String dbName, String cacheName, double min_support)
	{
		int quiet = 0;
		
		System.out.print("mining database " + dbName + "..."); 
		
		long start_time, end_time; 
		
		try
		{
			
			ArrayList frequent_itemsets;
			ArrayList  columnNames = null;
			
			BufferedWriter out = new BufferedWriter(new FileWriter("fim.txt")); 
			
			// Initialize algorithms
			FrequentItemsetsMiner alg = new FPgrowth(); 
			//FrequentItemsetsMiner alg = new Apriori(); 
			
			long numRows = 0;
			
			Timer timer_fi = null;
			
			// First step: find frequent itemsets
			DBReader dbr = new DBReader(dbName);
			DBCacheWriter dbcw = new DBCacheWriter(cacheName);
			
			start_time = System.currentTimeMillis(); 
			timer_fi = new Timer();
			timer_fi.start();
			alg.findFrequentItemsets(dbr, dbcw, min_support);
			timer_fi.stop();
			end_time = System.currentTimeMillis(); 
			
			System.out.println("total runtime: " + ((end_time-start_time)/1000)); 
			
			numRows = dbr.getNumRows();
			
			columnNames = dbr.getColumnNames();
			
			dbr.close();
			dbcw.close();
			
			// Second step: display large itemsets
			// read the contents of the cache in a SET
			DBCacheReader dbcr = new DBCacheReader(cacheName);
			
			SET frequents = new SET();
			SET.initializeSET(frequents, min_support, dbcr);
			dbcr.close();
			
			// get and display the itemsets from the SET (MR)
			
			frequent_itemsets = frequents.getItemsets();
			
			// write out all frequent itemsets 
			for (int i = 0; i < frequent_itemsets.size(); i++) 
			{
				Itemset FI = (Itemset) frequent_itemsets.get(i);
				String itemName = ""; 
				for (int j = 0; j < FI.size(); j++) 
				{
					int item = FI.get(j);
					itemName += (String)columnNames.get(item-1) + " ";
				}
				out.write(itemName + "\t" + (int) Math.ceil(FI.getSupport() * numRows) + "\n");
			}
		}
		catch (Exception e)
		{
			System.err.println("ERROR! " + e);
		}
	}	
}
