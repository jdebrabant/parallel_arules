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
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileRecordReader;

public class WholeSplitInputFormat extends FileInputFormat<NullWritable, TextArrayWritable> 
{
	@Override
	protected boolean isSplitable(FileSystem fs, Path filename)
	{
		return true;
	}

	@Override
	public RecordReader<NullWritable, TextArrayWritable> getRecordReader(
			InputSplit split, JobConf job, Reporter reporter) throws IOException
	{
		return new WholeSplitRecordReader((FileSplit) split, job);
	}

	class WholeSplitRecordReader implements RecordReader<NullWritable, TextArrayWritable>
	{
		private FileSplit fileSplit;
		private Configuration conf;
		private boolean processed = false;

		public WholeSplitRecordReader(FileSplit fileSplit, Configuration conf)
			throws IOException
		{
			this.fileSplit = fileSplit;
			this.conf = conf;
		}

		@Override
		public NullWritable createKey()
		{
			return NullWritable.get();
		}

		@Override
		public TextArrayWritable createValue()
		{
			return new TextArrayWritable();
		}

		@Override
		public long getPos() throws IOException
		{
			return processed ? fileSplit.getLength() : 0;
		}

		@Override
		public float getProgress() throws IOException
		{
			return processed ? 1.0f : 0.0f;
		}

		@Override
		public boolean next(NullWritable key, TextArrayWritable value) throws IOException
		{
			if (!processed)
			{
				ArrayList<Text> transactionsList = new ArrayList<Text>();
				SequenceFileRecordReader<LongWritable,Text> reader = new SequenceFileRecordReader<LongWritable,Text>(conf, fileSplit);
				LongWritable transactionID = reader.createKey();
				Text transaction = reader.createValue();
				while (reader.next(transactionID, transaction))
				{
					transactionsList.add(transaction);
					transaction = reader.createValue();
				}
				Text[] transactionArr = new Text[transactionsList.size()];
				transactionArr = transactionsList.toArray(transactionArr);
				value.set(transactionArr);
				processed = true;
				return true;
			}
			return false;
		}

		@Override
		public void close() throws IOException {
			// do nothing
		}
	}
}

