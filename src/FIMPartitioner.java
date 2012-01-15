import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Partitioner;

public class FIMPartitioner implements Partitioner<LongWritable, Text> {
	@Override
	public void configure(JobConf job) {}

	@Override
	public int getPartition(LongWritable key, Text value, int numPartitions) 
	{
		return (int)key.get();
	}
}

