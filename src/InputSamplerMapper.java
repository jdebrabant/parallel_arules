/*
 * Copyright 2012-14 Justin A. Debrabant <debrabant@cs.brown.edu> and Matteo Riondato <matteo@cs.brown.edu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
	private int id;
	private MapWritable map;
		
	@Override
	public void configure(JobConf conf) 
	{ 
		id = conf.getInt("mapred.task.partition", -1);
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
			OutputCollector<IntWritable, Text> output, Reporter
			reporter) throws IOException
	{
		reporter.incrCounter("FIMMapperStart", String.valueOf(id), System.currentTimeMillis());
	  	IntArrayWritable arr = (IntArrayWritable) map.get(key);
		if (arr != null) 
		{
		  	for(Writable element : arr.get()) 
			{
				output.collect((IntWritable) element, value);
			}
		}
		reporter.incrCounter("FIMMapperEnd", String.valueOf(id), System.currentTimeMillis());
	}
}

