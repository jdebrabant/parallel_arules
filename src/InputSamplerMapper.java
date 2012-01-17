import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import org.apache.hadoop.fs.FileSystem;
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

public class InputSamplerMapper extends MapReduceBase implements
	Mapper<LongWritable, Text, IntWritable, Text>
	{
		private MapFile.Reader reader;
		private FileSystem fs;
		
		@Override
		public void configure(JobConf conf) 
		{ 
			try {
				fs = FileSystem.getLocal(conf);
				reader = new MapFile.Reader(fs, ".", conf);
			} catch (IOException e) 
			{ 
			  	System.out.println(e.getMessage());
			} 
		}
		
		@Override
		public void map(LongWritable key, Text value,
						OutputCollector<IntWritable, Text> output,
						Reporter reporter) throws IOException
		{
			IntArrayWritable arr = new IntArrayWritable();
			if (reader.get(key, arr) != null)
			{
				for(Writable element : arr.get()) 
				{
					output.collect((IntWritable) element, value);
				}
			}
		}
		
		@Override
		public void close() throws IOException {
			fs.close();
		}
		
	}

