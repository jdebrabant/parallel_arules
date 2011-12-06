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


public class ParallelFrequentItemsetMiner
{

	public static final int REDUCER_NUM = 10; 
	
	//public static class Map extends org.apache.hadoop.mapred.Mapper
	public static class Map extends org.apache.hadoop.mapreduce.Mapper<Object, Text, LongWritable, Text>
	{
		@Override
		public void map(Object lineNum, Text value, Context context) throws IOException
		{
			Random rand = new Random();
			
			try
			{
				int key = (int)(rand.nextInt() % REDUCER_NUM);
				context.write(new LongWritable(key), value);
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage()); 
			}
		}
	}
	
	//public static class Reduce extends org.apache.hadoop.mapred.Reducer
	public static class Reduce extends org.apache.hadoop.mapreduce.Reducer<LongWritable, Text, Text, IntWritable>
	{
		@Override
		public void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException
		{
			//ArrayList frequent_itemsets; 
			
			int count = 0; 
			
			try 
			{
				Path output_file_path = null; 
				
				Path db_path; 
				Path cache_path; 
				Path asc_path; 
				
				String asc_file; 
				String db_file; 
				String cache_file; 

				asc2db db_converter = new asc2db(); 
				
				output_file_path = org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getWorkOutputPath(context);
				output_file_path = output_file_path.suffix("/temp"); 
							
				asc_file = org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getUniqueFile(context, "temp", ".asc"); 
				db_file = org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getUniqueFile(context, "temp", ".db");
				cache_file = org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getUniqueFile(context, "temp", ".cache");

				db_path = output_file_path.suffix(".db"); 
				cache_path = output_file_path.suffix(".cache"); 
				asc_path = output_file_path.suffix(".asc"); 					 
							
				//convertToASC(values, asc_path.toString()); 
				convertToASC(values, asc_file); 				

				// will read temp.asc from disk and write a file in .db format to temp.db
				//db_converter.convertToDB(asc_path.toString(), db_path.toString()); 
				db_converter.convertToDB(asc_file, db_file); 
				
				//mineDB(db_path.toString(), cache_path.toString(), 0, context); 
				mineDB(db_file, cache_file, 0, context);
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage()); 
			}
			
		}
		
		private static void convertToASC(Iterable<Text> values, String output_file)
		{
			
			BufferedWriter out; 
			
			String transaction; 
			
			StringTokenizer tokenizer; 
			
			String token; 
			
			int current, max = 0; 
			
			try 
			{		

				out = new BufferedWriter(new FileWriter(output_file)); 
				
				for(Text v : values)
				//for(String v: values)
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
				
				for(int i = 1; i <= max; i++) // write the header
				{
					out.write(i + " C" + i + "\n"); 
				}
				
				out.write("BEGIN_DATA" + "\n"); 
				
				for(Text v : values)
				//for(String v: values)				
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
		
		private static void mineDB(String dbName, String cacheName, double min_support, Context context)
		//private static void mineDB(String dbName, String cacheName, double min_support)
		{
			int quiet = 0;
			
			System.out.print("mining database " + dbName + "..."); 
			
			try
			{
				ArrayList frequent_itemsets;
				ArrayList  columnNames = null;

				
				// Initialize algorithms
				FrequentItemsetsMiner alg = new FPgrowth(); 
				
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

				System.out.println("...finished mining database."); 
				
				// Second step: display large itemsets
				// read the contents of the cache in a SET
				DBCacheReader dbcr = new DBCacheReader(cacheName);
				
				SET frequents = new SET();
				SET.initializeSET(frequents, min_support, dbcr);
				dbcr.close();
				
				// get and display the itemsets from the SET (MR)
				
				System.out.println("(" + numRows + ")");
				frequent_itemsets = frequents.getItemsets();
				
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
					
					if(i % 100 == 0)
						context.setStatus("just added <" + itemName + ", " + ((int)Math.ceil(FI.getSupport() * numRows)) + "> to output"); 
					
					// write Key/Value pair to reducer output
					context.write(new Text(itemName), new IntWritable((int)Math.ceil(FI.getSupport() * numRows))); 
				}
				
				System.out.println("done"); 
				
			}
			catch (Exception e)
			{
				System.err.println("ERROR! " + e);
			}
		}
	}

/*
		private static void convertToASC(Iterable<String> values, String output_file)
		{
			
			BufferedWriter out; 
			
			String transaction; 
			
			StringTokenizer tokenizer; 
			
			String token; 
			
			int current, max = 0; 
			
			try 
			{		

				out = new BufferedWriter(new FileWriter(output_file)); 
				
				//for(Text v : values)
				for(String v: values)
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
				
				for(int i = 1; i <= max; i++) // write the header
				{
					out.write(i + " C" + i + "\n"); 
				}
				
				out.write("BEGIN_DATA" + "\n"); 
				
				//for(Text v : values)
				for(String v: values)				
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

		//private static void mineDB(String dbName, String cacheName, double min_support, Context context)
		private static void mineDB(String dbName, String cacheName, double min_support)
		{
			int quiet = 0;
			
			System.out.print("mininging database " + dbName + "..."); 
			
			try
			{
				ArrayList frequent_itemsets;
				ArrayList  columnNames = null;

				
				// Initialize algorithms
				FrequentItemsetsMiner alg = new FPgrowth(); 
				
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
				
				// Second step: display large itemsets
				// read the contents of the cache in a SET
				DBCacheReader dbcr = new DBCacheReader(cacheName);
				
				SET frequents = new SET();
				SET.initializeSET(frequents, min_support, dbcr);
				dbcr.close();
				
				// get and display the itemsets from the SET (MR)
				
				System.out.println("(" + numRows + ")");
				frequent_itemsets = frequents.getItemsets();
				
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
					
					//if(1 % 100 == 0)
					//	context.setStatus("just added <" + itemName + ", " + ((int)Math.ceil(FI.getSupport() * numRows)) + "> to output"); 
					
					// write Key/Value pair to reducer output
					//context.write(new Text(itemName), new IntWritable((int)Math.ceil(FI.getSupport() * numRows))); 
				}
				
				System.out.println("done"); 
				
			}
			catch (Exception e)
			{
				System.err.println("ERROR! " + e);
			}
		}

	public static void test(String in_file, String out_file)
	{
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(in_file)); 
			ArrayList<String> values = new ArrayList<String>(); 

			String line; 

			while((line = in.readLine()) != null)
			{
				values.add(new String(line)); 
			}

			asc2db db_converter = new asc2db(); 
				 					
			convertToASC(values, "temp.asc"); 
				
			// will read temp.asc from disk and write a file in .db format to temp.db
			db_converter.convertToDB("temp.asc", "temp.db"); 
				
				
			mineDB("temp.db", "temp.cache", 0);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		} 
	}
*/
	
	public static void main(String args[])
	{
		if (args.length != 2)
		{
			System.out.println("usage: <path to input database> <path to output files>");
			System.exit(1); 
		}
		
		try 
		{
			/*
			JobConf conf = new JobConf(ParallelFrequentItemsetMiner.class); 
			conf.setJobName("parall FIM"); 
			
			conf.setOutputKeyClass(Text.class); 
			conf.setOutputValueClass(IntWritable.class); 
			
			conf.setMapperClass(Map.class); 
			conf.setReducerClass(Reduce.class); 
			
			conf.setInputPaths(conf, new Path(args[0])); 
			conf.setOutputPath(conf, new Path(args[1])); 
			
			JobClient.runJob(conf); 
			 */
				
			//test(args[0], args[1]); 		
			
			Job job = new Job(new Configuration());
			
			job.setJarByClass(ParallelFrequentItemsetMiner.class);
			
			job.setMapperClass(Map.class);
			job.setReducerClass(Reduce.class);
			
			
			FileInputFormat.setInputPaths(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));
			
			job.waitForCompletion(true);
			
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
}





