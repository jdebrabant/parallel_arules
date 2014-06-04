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
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import com.google.common.collect.Sets;

public class DistribCountingMapper extends MapReduceBase 
	implements Mapper<LongWritable, Text,Text, IntWritable>
{
	@Override
	public void map(LongWritable lineNum, Text value,
			OutputCollector<Text, IntWritable> output, 
			Reporter reporter) throws IOException
	{
	  	IntWritable one = new IntWritable(1);
	  	HashSet<String> transactionItems = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(value.toString());
		while (st.hasMoreTokens())
		{
		  	transactionItems.add(st.nextToken());
		}

	  	Set<Set<String>> powerSet = Sets.powerSet(transactionItems);
		for (Set<String> itemset : powerSet)
		{
		  	if (itemset.size() > 0) 
			{
				String[] itemsetArr = new String[itemset.size()];
				itemset.toArray(itemsetArr);
				Arrays.sort(itemsetArr);
				String itemsetStr = "";
				for (int i = 0; i < itemsetArr.length; i++)
				{
					itemsetStr += itemsetArr[i] + " ";	
				}
				output.collect(new Text(itemsetStr), one);
			}
		}
	}
}

