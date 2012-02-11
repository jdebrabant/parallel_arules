import java.io.IOException;
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
	private int reducersNum;
	private int toSample;

	@Override
	public void configure(JobConf conf) {
		reducersNum = conf.getInt("PARMM.reducersNum", 1000);
		try
		{
			IntWritable [] toSampleArr = DefaultStringifier.loadArray(conf, "PARMM.toSampleArr", IntWritable.class);
			int id = conf.getInt("mapred.task.partition", -1);
			toSample = toSampleArr[id].get();
			System.out.println(toSample);
		}
		catch (IOException e) {} 
	}
	
	@Override
	public void map(NullWritable lineNum, TextArrayWritable transactionsArrWr,
					OutputCollector<IntWritable, Text> output, 
					Reporter reporter) throws IOException
	{
		Random rand = new Random();
		Writable[] transactions = transactionsArrWr.get();
		int transactionsNum = transactions.length;
		for (int i = 0; i < toSample; i++)
		{
			int sampledIndex = rand.nextInt(transactionsNum);
			output.collect(new IntWritable(i % reducersNum), (Text) transactions[sampledIndex]);
		}
	}
}

