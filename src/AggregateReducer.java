import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class AggregateReducer extends MapReduceBase 
	implements Reducer<Text, Text, Text, DoubleWritable>
{
	int reducersNum;
	int requiredNum;
	float epsilon;

	@Override
	public void configure(JobConf conf) 
	{
		reducersNum = conf.getInt("PARMM.reducersNum", 64);
		requiredNum = reducersNum / 2 + 1;
		epsilon = conf.getFloat("PARMM.epsilon", (float) 0.02);
	}



	@Override
	public void reduce(Text itemset, Iterator<Text> values, 
			OutputCollector<Text,DoubleWritable> output, 
			Reporter reporter) throws IOException
	{
		ArrayList<Double> valuesArrList = new ArrayList<Double>();
		while (values.hasNext()) 
		{
			String valueString = (values.next()).toString();
			valuesArrList.add(Double.parseDouble(valueString));
		}
		/**
		 * Only consider the itemset as "global frequent" if it
		 * appears among the "local frequent" itemsets at least
		 * requiredNum times.
		 */
		if (valuesArrList.size() >= requiredNum)
		{
			Double[] valuesArr = new Double[valuesArrList.size()];
			valuesArrList.toArray(valuesArr);
			Arrays.sort(valuesArr);

			/**
			 * Compute the smallest frequency interval containing
			 * requiredNum estimates of the frequency of the
			 * itemset. Use the center of this interval as global
			 * estimate for the frequency. The confidence interval
			 * is obtained by enlarging the above interva by epsilon
			 * / 2 on both sides.
			 *
			 * XXX It seems to me that  it would actually be enough to
			 * require that the interval contains REDUCER_NUM / 2 +
			 * 1 estimates, independently on requiredNum. MR
			 */
			double minIntervalLength = valuesArr[requiredNum - 1] - valuesArr[0];
			double estimatedFreq = valuesArr[0] + minIntervalLength / 2;
			double confIntervalLowBound = valuesArr[0] - epsilon / 2;
			double confIntervalUppBound = valuesArr[requiredNum -1] + epsilon / 2;
			for (int i = 1; i < valuesArr.length - requiredNum; i++)
			{
				double intervalLength = valuesArr[requiredNum + i] - valuesArr[i];
				if (intervalLength < minIntervalLength) 
				{
					minIntervalLength = intervalLength;
					estimatedFreq = valuesArr[i] + minIntervalLength / 2;
					confIntervalLowBound = valuesArr[i] - epsilon / 2;
					confIntervalUppBound = valuesArr[requiredNum + i] + epsilon / 2;
				}
			}
			
			/**
			 * XXX We need to fix this output to include the bounds
			 * for the confidence interval. MR
			 */
			output.collect(itemset, new DoubleWritable(estimatedFreq));
		} // end if (valuesArrList.size() >= requiredNum)
	}
}


