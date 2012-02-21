import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class AggregateMapper extends MapReduceBase 
implements Mapper<Text, DoubleWritable, Text, DoubleWritable>
{
	private int id;
	private boolean set;

	@Override
	public void configure(JobConf conf) 
	{
		id = conf.getInt("mapred.task.partition", -1);
		set = false;
	}


	@Override
	public void map(Text itemset, DoubleWritable freq,
			OutputCollector<Text, DoubleWritable> output, Reporter reporter) throws IOException
	{
		long startTime = System.currentTimeMillis();
		if (! set)
		{
			reporter.incrCounter("AggregateMapperStart", String.valueOf(id), startTime);
			reporter.incrCounter("AggregateMapperEnd", String.valueOf(id), startTime);
			set = true;
		}

		String itemsetStr = itemset.toString();
		StringTokenizer strTok = new StringTokenizer(itemsetStr);
		String[] items  = new String[strTok.countTokens()];
		int i = 0;
		while (i < items.length)
		{
			items[i] = strTok.nextToken();
			i++;
		}
		Arrays.sort(items);
		String sortedItemsetStr = "";
		for (i = 0; i < items.length -1; i++) 
		{
			sortedItemsetStr = sortedItemsetStr +  items[i] + " ";
		}
		sortedItemsetStr += items[i];
		Text sortedItemset = new Text(sortedItemsetStr);
		output.collect(sortedItemset, freq);

		long endTime = System.currentTimeMillis();
		reporter.incrCounter("AggregateMapperEnd", String.valueOf(id), endTime-startTime);
	}



}

