import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class MRAprioriReducer extends MapReduceBase 
  implements Reducer<Text, IntWritable, Text, DoubleWritable>
{
	public static final int REDUCER_NUM = 64;
	
	public static final int MIN_SUPPORT_PERCENT = 20; 

	public static final int DATASET_SIZE = 1000;

	@Override
	public void reduce(Text itemset, Iterator<IntWritable> values, 
			OutputCollector<Text,DoubleWritable> output, 
			Reporter reporter) throws IOException
	{			
	  	int sum = 0;
	  	while (values.hasNext()) 
	  	{
	    		sum += values.next().get();
	  	}

	  	if (sum >= DATASET_SIZE * MIN_SUPPORT_PERCENT / 100)
	  	{
		  	double freq = ((double) sum) / DATASET_SIZE;
		  	output.collect(itemset, new DoubleWritable(freq));
		}
	}
}



