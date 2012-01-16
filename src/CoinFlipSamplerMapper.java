import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.ReflectionUtils;

public class CoinFlipSamplerMapper extends MapReduceBase 
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
		Random rand = new Random();
		
		for (int i=0; i < reducersNum; i++)
		{
			double f = rand.nextDouble();
			if (f <= 1.0 / datasetSize)
			{
				output.collect(new IntWritable(i),
							   value);
			}
		}
	}
	}