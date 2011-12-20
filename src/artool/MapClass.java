import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class MapClass extends MapReduceBase 
	implements Mapper<LongWritable, Text, IntWritable, Text>
{
	/**
	 * XXX It would be great if we could set these parameters at runtime and
	 * then read them from the Configuration. MR
	 */
	public static final int REDUCER_NUM = 64; 
	
	@Override
	public void map(LongWritable lineNum, Text value,
			OutputCollector<IntWritable, Text> output, 
			Reporter reporter) throws IOException
	{
		Random rand = new Random();
		
			int key = rand.nextInt(REDUCER_NUM);
			output.collect(new IntWritable(key), value);
	}
}

