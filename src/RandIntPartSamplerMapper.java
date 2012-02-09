import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class RandIntPartSamplerMapper extends MapReduceBase 
implements Mapper<LongWritable, Text, IntWritable, Text>
{

	@Override
	public void configure(JobConf conf) {
		int reducersNum = conf.getInt("PARMM.reducersNum", 1000);
	}
	
	@Override
	public void map(LongWritable lineNum, Text value,
					OutputCollector<IntWritable, Text> output, 
					Reporter reporter) throws IOException
	{
	}
}

