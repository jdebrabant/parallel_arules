
import java.util.*; 
import java.io.*; 
import java.lang.Math;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;

import laur.dm.ar.*;
import laur.tools.Timer;

public class ReduceClass extends Reducer<IntWritable, Text, Text, IntWritable>
{
	@Override
	public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	{			
		int count = 0; 
		
		try 
		{
			Path output_file_path = null; 
			
			String asc_file; 
			String db_file; 
			String cache_file; 
			
			asc2db db_converter = new asc2db(); 
			
			String random_file; 
			
			output_file_path = org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getWorkOutputPath(context);

			Random rand = new Random();
			
			random_file = new String((new Integer(rand.nextInt(100000))).toString()); 
			
			asc_file = random_file + ".asc"; 
			db_file = random_file + ".db"; 
			cache_file = random_file + ".cache"; 
			
			//asc_file = org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getUniqueFile(context, "temp" + random_file, ".asc");
			//db_file = org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getUniqueFile(context, "temp" + random_file, ".db");
			//cache_file = org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getUniqueFile(context, "temp" + random_file, ".cache");
			
			//convertToASC(values, asc_path.toString()); 
			convertToASC(values, asc_file); 				
			
			try 
			{
				// will read temp.asc from disk and write a file in .db format to temp.db
				db_converter.convertToDB(asc_file, db_file); 
			}
			catch(Exception e)
			{
				System.err.println("Error converting from asc to db"); 
			}
			
			//mineDB(db_path.toString(), cache_path.toString(), 0, context); 
			mineDB(db_file, cache_file, .2, context);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
	
	public static void convertToASC(Iterable<Text> values, String output_file)
	{
		
		BufferedWriter out; 
		
		String transaction; 
		
		StringTokenizer tokenizer; 
		
		String token; 
		
		int current, max = 100; 
		
		try 
		{		
			
			 out = new BufferedWriter(new FileWriter(output_file)); 
			
			/*
			 for(Text v : values)
			 {
				 transaction = v.toString(); 
				 
				 tokenizer = new StringTokenizer(transaction, " "); 
			 
				 while(tokenizer.hasMoreTokens())
				 {
					 token = tokenizer.nextToken(); 
					 
					 current = Integer.parseInt(token); 
				 
					 if(current > max)
						 max = current; 
				 }
			 }
			 */
			
			for(int i = 1; i <= max; i++) // write the header
			{
				out.write(i + " " + i + "\n"); 
			}
			
			out.write("BEGIN_DATA" + "\n"); 
			
			for(Text v : values)
			{
				transaction = v.toString(); 
				
				out.write(transaction + "\n"); 
			}
			out.write("END_DATA" + "\n"); 
			
			out.close(); 				
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
	
	public static void mineDB(String dbName, String cacheName, double min_support, Context context)
	{
		int quiet = 0;
		
		System.out.print("mining database " + dbName + "..."); 
		
		try
		{
			
			ArrayList frequent_itemsets;
			ArrayList  columnNames = null;
			
			// Initialize algorithms
			FrequentItemsetsMiner alg = new FPgrowth(); 
			//FrequentItemsetsMiner alg = new Apriori(); 
			
			long numRows = 0;
			
			Timer timer_fi = null;
			
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
			
			System.out.println("done."); 
			
			// Second step: display large itemsets
			// read the contents of the cache in a SET
			DBCacheReader dbcr = new DBCacheReader(cacheName);
			
			SET frequents = new SET();
			SET.initializeSET(frequents, min_support, dbcr);
			dbcr.close();
			
			// get and display the itemsets from the SET (MR)
			
			frequent_itemsets = frequents.getItemsets();
			
			System.out.println("num rows mined: " + numRows);
			System.out.println("frequent itemsets found: " + frequent_itemsets.size()); 
			
			// write out all frequent itemsets 
			for (int i = 0; i < frequent_itemsets.size(); i++) 
			{
				Itemset FI = (Itemset) frequent_itemsets.get(i);
				String itemName = ""; 
				for (int j = 0; j < FI.size(); j++) 
				{
					int item = FI.get(j);
					itemName += (String)columnNames.get(item-1) + " ";
					//System.out.print(itemName + " ");
				}
				System.out.println(itemName + "(" + (int) Math.ceil(FI.getSupport() * numRows) + ")");
				
				// write Key/Value pair to reducer output
				context.write(new Text(itemName), new IntWritable((int)Math.ceil(FI.getSupport() * numRows))); 
			}
		}
		catch (Exception e)
		{
			System.err.println("ERROR! " + e);
		}
	}
}



