import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import cern.jet.random.Binomial;

public class BinomialSamplerMapper extends MapReduceBase 
	implements Mapper<LongWritable, Text, IntWritable, Text>
{
	/**
	 * XXX It would be great if we could set these parameters at runtime and
	 * then read them from the Configuration. MR
	 */
	public static final int REDUCER_NUM = 64; 
	public static final int DATASET_SIZE = 1000;
	
	@Override
	public void map(LongWritable lineNum, Text value,
			OutputCollector<IntWritable, Text> output, 
			Reporter reporter) throws IOException
	{
		for (int i=0; i < REDUCER_NUM; i++)
		{
			int sampledTimes = Binomial.staticNextInt(
					DATASET_SIZE / REDUCER_NUM, 1.0 / DATASET_SIZE);
			/**
			 * XXX I assume there is a better way of doing
			 * this, by only having one "message" sent to
			 * reducer i, for example by making "value" an
			 * object containing the fields "sampleTimes"
			 * and "value". MR
			 */
			for (int j=0; j < sampledTimes; j++)
			{
				output.collect(new IntWritable(i), value);
			}
		}
	}
}

