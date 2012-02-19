import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileRecordReader;
import org.apache.hadoop.mapred.lib.CombineFileInputFormat;
import org.apache.hadoop.mapred.lib.CombineFileRecordReader;
import org.apache.hadoop.mapred.lib.CombineFileSplit;

public class CombineSequenceFileInputFormat extends CombineFileInputFormat<Text,DoubleWritable>
{

	public static class CombineSequenceFileRecordReader implements RecordReader<Text,DoubleWritable>
	{
		private final RecordReader<Text,DoubleWritable> reader;

		public CombineSequenceFileRecordReader(CombineFileSplit split,
		    Configuration conf, Reporter reporter, Integer index) throws IOException
		{
			FileSplit fileSplit = new FileSplit(split.getPath(index),
			      split.getOffset(index), split.getLength(index),
			      split.getLocations());
			reader = new SequenceFileRecordReader<Text,DoubleWritable>(conf, fileSplit);
		}

		@Override
		  public boolean next(Text key, DoubleWritable value) throws IOException
		  {
		  	return reader.next(key, value);
		  }

		@Override public Text createKey()
		{
		  	return reader.createKey();
		}

		@Override public DoubleWritable createValue()
		{
		  	return reader.createValue();
		}

		@Override public long getPos() throws IOException {
			return reader.getPos();
		}

		@Override public void close() throws IOException {
			reader.close();
		}

		@Override public float getProgress() throws IOException {
			return reader.getProgress();
		}
	}


  	@Override
	public RecordReader<Text,DoubleWritable> getRecordReader(InputSplit split, JobConf job, Reporter reporter)
	{
	  CombineFileRecordReader<Text,DoubleWritable> reader = null;
	  try
	  {
	    	reader = new CombineFileRecordReader<Text,DoubleWritable>(job, (CombineFileSplit) split, reporter, (Class) CombineSequenceFileRecordReader.class);
	  }
	  catch (IOException e) {}
	  return reader;
	}
}

