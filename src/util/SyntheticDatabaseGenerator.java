/**************************************************************************************************************************
 
 * File: SyntheticDatabaseGenerator.java
 * Authors: Justin A. DeBrabant (debrabant@cs.brown.edu)
			Matteo Riondato (matteo@cs.brown.edu)
 * Last Modified: 12/27/2011
 * Other Packages: 
	This data generator was written using the SyntheticDataGenerator utility in the laur.dm.ar package written by 
	Laurentiu Cristofor (laur@cs.umb.edu). 

 * Description:
	Synthetic Database Generator. Generates transactions based on user-specified parameters and writes transactions
	to an output file, 1 transaction per line. The parameters are as follows: 
		- num_transactions: total number of transactions in the database
		- avg transaction size: avg size (in number of items) of each transaction
		- number of patterns: total number of possible itemset patterns to choose from when creating each transaction 
		- avg pattern size: avg size (in number of items) of each itemset pattern
		- number of items: cardinality of the set of all possible items from which transactions will be created
		- correlation: correlation value, in [0.1], among patterns 
		- corruption: corrption value, in [0,1], for each pattern
 * Usage: java SyntheticDatabaseGenerator
 
 **************************************************************************************************************************/

import java.util.ArrayList;
import java.io.File;
import java.io.*;

import laur.dm.ar.*;

public class SyntheticDatabaseGenerator
{
	private static long num_transactions;
	private static double correlation;
	private static double corruption;
	private static int avg_transaction_size;
	private static int num_patterns;
	private static int avg_pattern_size;
	private static int num_items;
	private static String output_dir; 
	
	private static String db_name;
	
	private static void showUsage()
	{
		System.out.println("gendb v1.0 (C)2002 Laurentiu Cristofor\n\na synthetic database generator\n\nUsage:\n\njava gendb <num_transactions> <avg_transaction_size> <num_patterns> <avg_pattern_size> <num_items> <correlation> <corruption>");
		System.exit(0);
	}
	
	public static void main(String args[])
	{
		Console c = System.console();
		BufferedWriter db_out; 
		
		if(c == null)
		{
			System.out.println("ERROR: failed to read from console."); 
			System.exit(1); 
		}
		
		try
		{
			System.out.print("number of transactions: "); 
			num_transactions = Long.parseLong(c.readLine());
			if (num_transactions < 1)
			{
				System.out.println("ERROR: invalid number of transactions"); 
				System.exit(1); 
			}
			
			System.out.print("avg transaction size: "); 
			avg_transaction_size = Integer.parseInt(c.readLine());
			if (avg_transaction_size < 1)
			{
				System.out.println("ERROR: invalid avg transaction size"); 
				System.exit(1); 
			}
			
			System.out.print("number of patterns: ");
			num_patterns = Integer.parseInt(c.readLine());
			if (num_patterns < 1)
			{
				System.out.println("ERROR: invalid number of patterns"); 
				System.exit(1); 
			}
			
			System.out.print("avg pattern size: ");
			avg_pattern_size = Integer.parseInt(c.readLine());
			if (avg_pattern_size < 1)
			{
				System.out.println("ERROR: invalid avg pattern size"); 
				System.exit(1); 
			}
			
			System.out.print("number of items: "); 
			num_items = Integer.parseInt(c.readLine());
			if (num_items < 1 || num_items < avg_transaction_size || num_items < avg_pattern_size)
			{
				System.out.println("ERROR: invalid number of items"); 
				System.exit(1); 
			}
			
			System.out.print("correlation [0,1]: "); 
			correlation = Double.parseDouble(c.readLine());
			if (correlation < 0 || correlation > 1)
			{
				System.out.println("ERROR: invalid correlation"); 
				System.exit(1); 
			}
			
			System.out.print("corruption [0,1]: "); 
			corruption = Double.parseDouble(c.readLine());
			if (corruption < 0 || corruption > 1)
			{
				System.out.println("ERROR: invalid corruption"); 
				System.exit(1); 
			}
			
			System.out.print("output directory: "); 
			output_dir = c.readLine(); 
			
			db_name = output_dir + "/T" + num_transactions + "_AT" + avg_transaction_size
			+ "_P" + num_patterns + "_AP" + avg_pattern_size + "_I" + num_items + ".dat";
			
			db_out = new BufferedWriter(new FileWriter(db_name)); 
			
			System.out.print("initializing data generator..."); 
			
			SyntheticDataGenerator sdg = new SyntheticDataGenerator(num_transactions, avg_transaction_size,
																	num_patterns, avg_pattern_size,
																	num_items, correlation, corruption);
			System.out.println("done."); 
			
			System.out.print("generating synthetic database..."); 
			
			while (sdg.hasMoreTransactions())
			{
				db_out.write(sdg.getNextTransaction().toString() + "\n"); 
			}
			
			System.out.println("done."); 
			
			db_out.close();
		}
		catch(Exception e)
		{
			System.out.println("ERROR: " + e.getMessage()); 
		}
	}
}



