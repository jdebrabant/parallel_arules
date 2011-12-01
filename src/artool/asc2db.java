/*
 asc2db.java
 
 (P)2001 Laurentiu Cristofor
 */

/*
 
 ARtool - Association Rules tools
 Copyright (C) 2002  Laurentiu Cristofor
 
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or (at
 your option) any later version.
 
 This program is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 USA
 
 
 ARtool was written by Laurentiu Cristofor (laur@cs.umb.edu).
 
 */

import laur.dm.ar.*;

import java.io.*;
import java.util.*;

/**
 * This program converts an ASCII file to an ARtool database file. The
 * ASCII file must be formatted as follows:
 *
 * first section contains a listing of the table column names preceded
 * by their index starting from 1, each on a separate line. Example:
 *
 * <pre>
 * 1 apples
 * 2 oranges
 * 3 grapes
 * </pre>
 *
 * second section is opened by a BEGIN_DATA line and contains lists of
 * integers that identify the attributes participating in a
 * transaction/relation/tuple, each such tuple being represented by a
 * list followed by a new line, like in the following example:
 *
 * <pre>
 * BEGIN_DATA
 * 1 3
 * 2
 * 2 3
 * END_DATA
 * </pre>
 *
 * In the above example the first transaction contained apples and
 * grapes, the second contained oranges, and the third contained
 * oranges and grapes.
 *
 * this second section should be closed by an END_DATA line.
 *
 * @author	Laurentiu Cristofor
 * @version 	1.0, May 4th, 2001
 * */
public class asc2db
{
	private static final String BEGIN_DATA = "BEGIN_DATA";
	private static final String END_DATA = "END_DATA";
	
	public static void main(String[] args)
	{
		String inputFileName = null;
		String outputFileName = null;
		
		switch (args.length)
		{
			case 1:
				inputFileName = args[0] + ".asc";
				outputFileName = args[0] + ".db";
				break;
				
			case 2:
				inputFileName = args[0] + ".asc";
				outputFileName = args[1] + ".db";
				break;
				
			default:
				System.out.println("asc2db v1.0 (C)2001-2002 Laurentiu Cristofor\n\nThis program converts a .asc file to .db format.\n\nUsage: java asc2db inputFile <outputFile>\n\nNotes: the file extensions do not have to be specified,\n asc2db will assume a .asc extension for the input file\n and a .db extension for the output file.\n The output file name is optional, by default it will be\n the same as the input file name,\n just the extension being different.");
				System.exit(0);
		}
		
		convertToDB(inputFileName, outputFileName); 
	}
	
	public static void convertToDB(String inputFileName, String outputFileName)
	{
		File out = new File(outputFileName);
		if (out.exists())
		{
			System.out.println("Output database file already exists! Please select another output file.");
			System.exit(1);
		}
		
		try
		{
			String line;
			StringTokenizer strTok;
			ArrayList columnNames = new ArrayList();
			
			BufferedReader bufr
			= new BufferedReader(new FileReader(inputFileName));
			
			// first gather the column names
			int counter = 0;
			while (!(line = bufr.readLine()).equals(BEGIN_DATA))
			{
				counter++;
				strTok = new StringTokenizer(line);
				
				// there must be at least 2 tokens,
				// the column number and the column name.
				// there can be more tokens if the column name contains blanks!
				if (strTok.countTokens() < 2)
					if (line.length() > 0)
					{
						System.out.println("Invalid line:\n\t" + line 
										   + "\nProcessing is aborted!");
						bufr.close();
						System.exit(1);
					}
					else
					{
						// it's a blank line, ignore it and reset counter
						counter--;
						continue;
					}
				
				// get column number
				String number = strTok.nextToken();
				if (!number.equals("" + counter))
				{
					System.out.println("Column number out of order: " + number
									   + " found where " + counter 
									   + " was expected."
									   + "\nProcessing is aborted!");
					bufr.close();
					System.exit(1);
				}
				
				// get column name
				String columnName = strTok.nextToken("");
				columnNames.add(columnName);
			}
			
			// if we got here then the column names section was properly processed
			// and a BEGIN_DATA delimiter was found
			
			Itemset row;
			String str_item;
			int item;
			
			// we can now start processing the transactions up to the END_DATA
			// delimiter
			
			DBWriter dbw = new DBWriter(outputFileName);
			dbw.setColumnNames(columnNames);
			
			while (!(line = bufr.readLine()).equals(END_DATA))
			{
				strTok = new StringTokenizer(line);
				row = new Itemset();
				while (strTok.hasMoreTokens())
				{
					str_item = strTok.nextToken();
					item = Integer.parseInt(str_item);
					row.add(item);
				}
				
				if (row.size() > 0)
					dbw.addRow(row);
			}
			
			dbw.close();
			bufr.close();
			
			System.out.println("Conversion was successful!");
		}
		// readLine returns null when it finds EOF,
		// which will cause an EOFException to occur in the above code
		catch (NullPointerException e)
		{
			System.out.println("Unexpected EOF encountered!");
			System.exit(1);
		}
		catch (Exception e)
		{
			System.out.println("An error occurred: " + e);
			System.exit(1);
		}
		
	}
}
