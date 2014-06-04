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
import java.util.Arrays;
import java.util.StringTokenizer;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class AggregateMapper extends MapReduceBase 
implements Mapper<Text, DoubleWritable, Text, DoubleWritable>
{
	private int id;
	private boolean set;

	@Override
	public void configure(JobConf conf) 
	{
		id = conf.getInt("mapred.task.partition", -1);
		set = false;
	}


	@Override
	public void map(Text itemset, DoubleWritable freq,
			OutputCollector<Text, DoubleWritable> output, Reporter reporter) throws IOException
	{
		long startTime = System.currentTimeMillis();
		if (! set)
		{
			reporter.incrCounter("AggregateMapperStart", String.valueOf(id), startTime);
			reporter.incrCounter("AggregateMapperEnd", String.valueOf(id), startTime);
			set = true;
		}

		String itemsetStr = itemset.toString();
		StringTokenizer strTok = new StringTokenizer(itemsetStr);
		String[] items  = new String[strTok.countTokens()];
		int i = 0;
		while (i < items.length)
		{
			items[i] = strTok.nextToken();
			i++;
		}
		Arrays.sort(items);
		String sortedItemsetStr = "";
		for (i = 0; i < items.length -1; i++) 
		{
			sortedItemsetStr = sortedItemsetStr +  items[i] + " ";
		}
		sortedItemsetStr += items[i];
		Text sortedItemset = new Text(sortedItemsetStr);
		output.collect(sortedItemset, freq);

		long endTime = System.currentTimeMillis();
		reporter.incrCounter("AggregateMapperEnd", String.valueOf(id), endTime-startTime);
	}



}

