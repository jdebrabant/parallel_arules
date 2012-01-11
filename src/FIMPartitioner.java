import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Partitioner;

public class FIMPartitioner implements Partitioner<IntWritable, Text> {
	@Override
	public void configure(JobConf job) {}

	@Override
	public int getPartition(IntWritable key, Text value, int numPartitions) {
		return key.get();
	}
}

