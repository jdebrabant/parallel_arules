/********************************************************************************
 File: FPgrowth.java
 Author(s): Justin A. DeBrabant (debrabant@cs.brown.edu)
 Description: 
	Main application for frequent itemset mining using the FP-growth algorithm. 
 Parameters: 
	-F input file
	-S support value (expressed as a percent, defaults to 20%)
	-C confidence value (expressed as a percent, defaults to 80%)
	-O output file name (defaults to frequent_itemsets.txt)
*********************************************************************************/

package fim.fpgrowth; 

import java.io.*;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.DoubleWritable;


public class FPgrowth
{
	private boolean verbose = false; 
	
    public static void main(String[] args) throws IOException 
	{
        mineFrequentItemsets(args, true, null); 
	}
	
	/*
	 Method: mineFrequentItemsets
	 Description: 
		User-level function to handle all aspects of frequent itemset mining. Can operate in either "local" or "hadoop" 
		mode, depending on whether the local flag is set. In local mode, all output (i.e. frequent itemsets), will be 
		written to the file specified with the -O command line argument. In distributed mode, all output will be emitted
		as key/value pairs using the OutputCollector object provided. 
	 Parameters: 
		- args: an array of String objects representing command line arguments (-F, -S, -C, -O). For example, to set the 
				output file (-O), the String object would be "-Omy_output.txt". 
		- local: sets the execution mode, true for local (writes to local file), 
				 false for distributed (emits Hadoop key/value pairs). 
		- output: an OutputCollector object to emit key/value pairs. If running local mode, set this to null. 
	 */
	public static void mineFrequentItemsets(String [] args, boolean local, OutputCollector<Text,DoubleWritable> output)
	{
		long start_time, end_time; 
		double total_time; 
		
		FPtree newFPtree = new FPtree(args);
		
		// Read data to be mined from file
		//System.out.print("reading transaction data to be mined..."); 
		start_time = System.currentTimeMillis(); 
		newFPtree.inputDataSet();
		end_time = System.currentTimeMillis();
		total_time = newFPtree.twoDecPlaces((end_time - start_time) / 1000.0); 
		//System.out.println("done (" + total_time + " seconds)");

		
		// Reorder and prune input data according to frequency of single attributes	
		System.out.print("pruning items with low support..."); 
		newFPtree.idInputDataOrdering();
		newFPtree.recastInputDataAndPruneUnsupportedAtts(); 
		newFPtree.setNumOneItemSets();
		end_time = System.currentTimeMillis();
		total_time = newFPtree.twoDecPlaces((end_time - start_time) / 1000.0); 
		System.out.println("done (" + total_time + " seconds)");
		//newFPtree.outputDataArray();
		
		
        // Build initial FP-tree
		System.out.print("building FP-tree..."); 
		start_time = System.currentTimeMillis(); 
		newFPtree.createFPtree();
		end_time = System.currentTimeMillis();
		total_time = ((end_time - start_time) / 1000.0); 
		System.out.println("done (" + total_time + " seconds)");
		//newFPtree.outputFPtreeStorage();			
		//newFPtree.outputFPtree();
		//newFPtree.outputItemPrefixSubtree();
		
		
		// Mine FP-tree
		System.out.print("mining FP-tree..."); 
		start_time = System.currentTimeMillis(); 
		newFPtree.startMining();
		end_time = System.currentTimeMillis();
		total_time = newFPtree.twoDecPlaces((end_time - start_time) / 1000.0); 
		System.out.println("done (" + total_time + " seconds)");
		
		//newFPtree.outputFrequentSets(); 
		//newFPtree.outputStorage(); 
		//newFPtree.outputNumFreqSets(); 
		//newFPtree.outputTtree(); // Frequent sets are stored in this structure
		//newFPtree.outputRules();	
		
		if(local)
		{
			newFPtree.writeFrequentSets(); 
		}
		else 
		{
			if(output == null)
			{
				System.out.println("ERROR: OutputCollector cannot be null in distributed mode"); 
				System.exit(1); 
			}
			newFPtree.emitFrequentSets(output); 
		}
		
	}
}







    
