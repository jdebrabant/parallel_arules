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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

public class Txt2SeqConverter
{
	public static void main(String[] args)
	{
	  	if (args.length != 2) 
		{
		  	System.out.println("Usage: env HADOOP_CLASSPATH=.:$HADOOP_CLASSPATH hadoop Txt2SeqConverter input output");
			System.exit(1);
		}
	  	FileSystem fs = null;
	  	String seqFileName = args[1];
		Configuration conf = new Configuration();
		try {
		  	fs = FileSystem.get(URI.create(seqFileName), conf);
		} catch (IOException e)
		{
			System.out.println("ERROR: " + e.getMessage()); 
		}
		  
		Path path = new Path(seqFileName);

		LongWritable key = new LongWritable();
		Text value = new Text();
		SequenceFile.Writer writer = null;
		try
		{
		  	writer = SequenceFile.createWriter(fs, conf, path, LongWritable.class, Text.class);
		  	//writer = SequenceFile.createWriter(fs, conf, path, LongWritable.class, Text.class, SequenceFile.CompressionType.BLOCK, new com.hadoop.compression.lzo.LzoCodec());
			BufferedReader br = new BufferedReader(new FileReader(args[0]));

			int transactionID = 0;
			String transaction = null;
			while ((transaction = br.readLine()) != null)
			{
			  key.set(transactionID);
			  value.set(transaction);
			  writer.append(key, value);

			  transactionID++;
			}
		} 
		catch (IOException e) 
		{
			System.out.println("ERROR: " + e.getMessage()); 
		} 
		finally 
		{
		  	IOUtils.closeStream(writer);
		}
	}
}

