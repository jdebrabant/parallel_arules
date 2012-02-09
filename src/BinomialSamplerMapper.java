import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import cern.jet.random.Binomial;

public class BinomialSamplerMapper extends MapReduceBase 
implements Mapper<LongWritable, Text, IntWritable, Text>
{
	private int reducersNum;
	private int datasetSize;
	
	@Override
	public void configure(JobConf conf) {
		reducersNum = conf.getInt("PARMM.reducersNum", 64);
		datasetSize = conf.getInt("PARMM.datasetSize", 1000);
	}
	
	
	
	@Override
	public void map(LongWritable lineNum, Text value,
					OutputCollector<IntWritable, Text> output, 
					Reporter reporter) throws IOException
	{
		for (int i=0; i < reducersNum; i++)
		{
			int sampledTimes = Binomial.staticNextInt(
													  datasetSize / reducersNum, 
													  1.0 / datasetSize);
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
