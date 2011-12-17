/*************************************************************************************
 - File: dat2asc.java
 - Author: Justin A. DeBrabant (debrabant@cs.brown.edu)
 - Class: dat2asc
 - Description: 
	 Tool of the artool package to convert files between "dat" files to "asc" files 
	 by adding header information. 
 **************************************************************************************/

import java.util.*; 
import java.io.*; 

public class dat2asc
{	
	public static void main(String [] args)
	{
		if(args.length != 2)
		{
			System.out.println("Usage: java dat2asc <input asc file> <output data file>"); 
			System.exit(1); 
		}
		
		convertToASC(args[0], args[1]); 
	}
	
	public static void convertToASC(String input_file, String output_file)
	{
		BufferedReader in;
		BufferedWriter out; 
		
		StringTokenizer tokenizer; 
		
		String line; 
		String token; 
		
		int max = 0, current;  
				
		try 
		{
			in = new BufferedReader(new FileReader(input_file)); 
			out = new BufferedWriter(new FileWriter(output_file)); 
			
			while((line = in.readLine()) != null) // read through the file, line by line
			{
				tokenizer = new StringTokenizer(line, " "); 
				
				while(tokenizer.hasMoreTokens())
				{
					token = tokenizer.nextToken(); 
					
					current = Integer.parseInt(token); 
					
					if(current > max)
						max = current; 
				}
			}
			
			in.close();
			
			// reopen file 
			in = new BufferedReader(new FileReader(input_file)); 
			
			// write the header
			for(int i = 1; i <= max; i++)
			{
				out.write(i + " C" + i + "\n"); 
			}
			
			out.write("BEGIN_DATA" + "\n"); 
			
			while((line = in.readLine()) != null)
			{
				out.write(line + "\n"); 
			}
			out.write("END_DATA" + "\n"); 
			
			
			in.close(); 
			out.close(); 
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
	
}