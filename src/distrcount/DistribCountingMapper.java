import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import com.google.common.collect.Sets;

public class DistribCountingMapper extends MapReduceBase 
	implements Mapper<LongWritable, Text,Text, IntWritable>
{
	@Override
	public void map(LongWritable lineNum, Text value,
			OutputCollector<Text, IntWritable> output, 
			Reporter reporter) throws IOException
	{
	  	IntWritable one = new IntWritable(1);
	  	HashSet<String> transactionItems = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(value.toString());
		while (st.hasMoreTokens())
		{
		  	transactionItems.add(st.nextToken());
		}

	  	Set<Set<String>> powerSet = Sets.powerSet(transactionItems);
		for (Set<String> itemset : powerSet)
		{
		  	if (itemset.size() > 0) 
			{
				String[] itemsetArr = new String[itemset.size()];
				itemset.toArray(itemsetArr);
				Arrays.sort(itemsetArr);
				String itemsetStr = "";
				for (int i = 0; i < itemsetArr.length; i++)
				{
					itemsetStr += itemsetArr[i] + " ";	
				}
				output.collect(new Text(itemsetStr), one);
			}
		}
	}
}

