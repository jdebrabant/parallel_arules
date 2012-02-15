import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.apache.hadoop.io.DefaultStringifier;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class RandIntPartSamplerMapper extends MapReduceBase 
implements Mapper<NullWritable, TextArrayWritable, IntWritable, Text>
{
	private int id;
	private int reducersNum;
	private int toSample;
	private IntWritable[] sampleDestinations;

	@Override
	public void configure(JobConf conf) {
		id = conf.getInt("mapred.task.partition", -1);
		reducersNum = conf.getInt("PARMM.reducersNum", 1000);
		try
		{
			int id = conf.getInt("mapred.task.partition", -1);
			System.out.println("id: " + id);
			IntWritable[] toSampleArr = DefaultStringifier.loadArray(conf, "PARMM.toSampleArr_" + id, IntWritable.class);
			toSample = 0;
			for (IntWritable toSampleRed : toSampleArr)
			{
				toSample += toSampleRed.get();
			}
			System.out.println("toSample: " + toSample);
			sampleDestinations = new IntWritable[toSample];
			int i = 0;
			for (int k = 0; k < toSampleArr.length; k++)
			{
				for (int j = 0; j < toSampleArr[k].get(); j++)
				{
					sampleDestinations[i++] = new IntWritable(k);
				}
			}
			Collections.shuffle(Arrays.asList(sampleDestinations));
		}
		catch (IOException e) {} 
	}
	
	@Override
	public void map(NullWritable lineNum, TextArrayWritable transactionsArrWr,
					OutputCollector<IntWritable, Text> output, 
					Reporter reporter) throws IOException
	{
		reporter.incrCounter("FIMMapperStart", String.valueOf(id), System.currentTimeMillis());
		Random rand = new Random();
		Writable[] transactions = transactionsArrWr.get();
		int transactionsNum = transactions.length;
		System.out.println("transactionsNum: " + transactionsNum);
		for (int i = 0; i < toSample; i++)
		{
			int sampledIndex = rand.nextInt(transactionsNum);
			output.collect(sampleDestinations[i], (Text) transactions[sampledIndex]);
		}
		reporter.incrCounter("FIMMapperEnd", String.valueOf(id), System.currentTimeMillis());
	}
}

