import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import fim.fpgrowth.*; 

public class FIMReducer extends MapReduceBase implements Reducer<IntWritable, Text, Text, DoubleWritable>
{
	public static final int REDUCER_NUM = 64;
	
	public static final int MIN_SUPPORT_PERCENT = 20; 

	@Override
	public void reduce(IntWritable key, Iterator<Text> values, 
			OutputCollector<Text,DoubleWritable> output, 
			Reporter reporter) throws IOException
	{			
		int count = 0; 
		
		BufferedWriter dat_out; 
		String random_file; 
		Random rand;
		String transaction; 
		
		String [] args = new String[2]; 
			
		try 
		{
			// create random file name to write transaction data to
			rand = new Random(); 
			random_file = new String((new Integer(rand.nextInt(100000))).toString()); 
			random_file += ".dat"; 
			
			// open file with random file name
			dat_out = new BufferedWriter(new FileWriter(random_file)); 
			
			// iterate through key/value pairs and write out transaction, 1 per line
			while (values.hasNext())
			{
				Text v = values.next();
				transaction = v.toString(); 
				
				dat_out.write(transaction + "\n"); 
			}
			
			// create command line arguments to pass to frequent itemset miner
			args[0] = new String("-F" + random_file);			// arg for input file
			args[1] = new String("-S" + MIN_SUPPORT_PERCENT);	// arg for minimum support
			
			// mine frequent itemsets
			FPgrowth.mineFrequentItemsets(args, false, output);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
}



