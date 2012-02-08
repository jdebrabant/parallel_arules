import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class MRAprioriReducer extends MapReduceBase 
  implements Reducer<Text, IntWritable, Text, Text>
{
	int minFreqPercent;
	int datasetSize;

	@Override
	public void configure(JobConf conf) 
	{
		minFreqPercent = conf.getInt("MRAPRIORI.minFreqPercent", 20); 
		datasetSize = conf.getInt("MRAPRIORI.datasetSize", 1000);
	}
	@Override
	public void reduce(Text itemset, Iterator<IntWritable> values, 
			OutputCollector<Text,Text> output, 
			Reporter reporter) throws IOException
	{			
	  	int sum = 0;
	  	while (values.hasNext()) 
	  	{
	    		sum += values.next().get();
	  	}

	  	if (sum >= datasetSize * minFreqPercent / 100)
	  	{
		  	double freq = ((double) sum) / datasetSize;
		  	output.collect(itemset, new Text((new Double(freq)).toString()));
		}
	}
}



