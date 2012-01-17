import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.ReflectionUtils;

public class InputSamplerMapper extends MapReduceBase implements
	Mapper<LongWritable, Text, IntWritable, Text>
	{
		private MapWritable map;
		
		@Override
		public void configure(JobConf conf) 
		{ 
			try {
				map = new MapWritable();
				Path[] localFiles = DistributedCache.getLocalCacheFiles(conf);
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(localFiles[0].toString()));
				map.readFields(new DataInputStream(in));
			} catch (IOException e) 
			{ 
			  	System.err.println(e.getMessage());
			} 
		}
		
		@Override
		public void map(LongWritable key, Text value,
						OutputCollector<IntWritable, Text> output,
						Reporter reporter) throws IOException
		{
		  	IntArrayWritable arr = (IntArrayWritable) map.get(key);
			if (arr != null) 
			{
			  	for(Writable element : arr.get()) 
				{
					output.collect((IntWritable) element, value);
				}
			}
		}
	}

