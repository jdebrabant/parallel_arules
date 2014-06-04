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

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class DistribCountingReducer extends MapReduceBase 
  implements Reducer<Text, IntWritable, Text, Text>
{
	int minFreqPercent;
	int datasetSize;

	@Override
	public void configure(JobConf conf) 
	{
		minFreqPercent = conf.getInt("DISTRCOUNT.minFreqPercent", 20); 
		datasetSize = conf.getInt("DISTRCOUNT.datasetSize", 1000);
	}
	@Override
	public void reduce(Text itemset, Iterator<IntWritable> values, 
			OutputCollector<Text,Text> output, 
			Reporter reporter) throws IOException
	{			
	  	int sum = 0;
	  	while (values.hasNext()) 
	  	{
	    		sum += values.next().get();
	  	}

	  	if (sum >= datasetSize * minFreqPercent / 100)
	  	{
		  	double freq = ((double) sum) / datasetSize;
		  	output.collect(itemset, new Text((new Double(freq)).toString()));
		}
	}
}



