/*************************************************************************************
 - File: asc2dat.java
 - Author: Justin A. DeBrabant (debrabant@cs.brown.edu)
 - Class: asc2dat
 - Description: 
	Tool of the artool package to convert files between "asc" files to "dat" files 
	by removing header information. 
 **************************************************************************************/

import java.util.*; 
import java.io.*; 

public class asc2dat
{
	
	public static void main(String [] args)
	{
		BufferedReader in;
		BufferedWriter out; 
		
		StringTokenizer tokenizer; 
		
		String line; 
		String token; 
		
		boolean is_transaction;
		
		if(args.length != 2)
		{
			System.out.println("Usage: java asc2dat <input asc file> <output data file>"); 
			System.exit(1); 
		}
		
		try 
		{
			in = new BufferedReader(new FileReader(args[0])); 
			out = new BufferedWriter(new FileWriter(args[1])); 
			
			is_transaction = false; 
			while((line = in.readLine()) != null) // read through the file, line by line
			{
				System.out.println("here"); 
				tokenizer = new StringTokenizer(line, " "); 
				
				token = tokenizer.nextToken(); 
				
				if(token.equals("BEGIN_DATA"))
				{
					is_transaction = true; 
					continue; 
				}
				else if(token.equals("END_DATA"))
				{
					is_transaction = false; 
					break; 
				}
				
				if(is_transaction)  // print out this line
				{
					out.write(line + "\n"); 
				}
			}
			
			in.close(); 
			out.close(); 
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
}


