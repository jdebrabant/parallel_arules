/*
  db2asc.java
  
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

/**
 * This program converts an ARtool database to an ASCII file,
 * formatted as follows:
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
public class db2asc
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
	inputFileName = args[0] + ".db";
	outputFileName = args[0] + ".asc";
	break;

      case 2:
	inputFileName = args[0] + ".db";
	outputFileName = args[1] + ".asc";
	break;

      default:
	System.out.println("db2asc v1.0 (C)2001-2002 Laurentiu Cristofor\n\nThis program converts a .db database to .asc format.\n\nUsage: java db2asc inputFile <outputFile>\n\nNotes: the file extensions do not have to be specified,\n db2asc will assume a .db extension for the input file\n and a .asc extension for the output file.\n The output file name is optional, by default it will be\n the same as the input file name,\n just the extension being different.");
	System.exit(0);
      }

    try
      {
	DBReader dbr = new DBReader(inputFileName);

	if (dbr.checkIntegrity() == false)
	  {
	    System.out.println("Input database is corrupted! Processing is aborted!");
	    dbr.close();
	    System.exit(1);
	  }

	BufferedWriter bufw 
	  = new BufferedWriter(new FileWriter(outputFileName));

	for (int i = 1; i <= dbr.getNumColumns(); i++)
	  {
	    bufw.write("" + i + " " + dbr.getColumnName(i - 1) + "\n");
	  }

	bufw.write(BEGIN_DATA + "\n");

	Itemset row;
	StringBuffer sb;
	while (dbr.hasMoreRows())
	  {
	    row = dbr.getNextRow();

	    sb = new StringBuffer();
	    for(int i = 0; i < row.size(); i++)
	      sb.append("" + row.get(i) + " ");
	    sb.append("\n");

	    bufw.write(sb.toString());
	  }

	bufw.write(END_DATA + "\n");

	dbr.close();
	bufw.close();
      }
    catch (Exception e)
      {
	System.out.println("An error occurred: " + e);
	System.exit(1);
      }
  }
}
