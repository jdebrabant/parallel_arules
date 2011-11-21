

import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputController;
import org.apache.hadoop.mapred.Reporter;

import laur.dm.ar.*;
import laur.tools.Timer;
import java.util.ArrayList;
import java.lang.Math;

public class ParallelFrequentItemsetMiner
{
	
	public class Map extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>
	{
		
		public void map(LongWritable lineNum, Text value, OutputCollecter<IntWritable, Text> output, Reporter reporter) throws IOException
		{
			int key = lineNum % REDUCER_NUM;
			output.collect(new IntWritable(key), value);
		}
	}
	
	class Reduce extends MapReduceBase implements Reducer<IntWritable, Text, Text, Text>
	{
		public class reduce()
		{
			
		}
		

	
		
		/*
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
		 */
		
		private static void mineDB(String dbName)
		{
			String dbName;
			String cacheName;
			
			try
			{
				// Initialize algorithms
				FrequentItemsetsMiner alg = new FPgrowth(); 
				
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
				
			}
			catch (Exception e)
			{
				System.out.println("ERROR! " + e);
			}
		}
	}
}





