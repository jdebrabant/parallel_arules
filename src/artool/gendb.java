/*
  gendb.java
 
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

import java.util.ArrayList;
import java.io.File;

/**
 * This program generates databases according to various input parameters.
 *
 * @version 	1.0
 * @author	Laurentiu Cristofor
 */
class gendb
{
  private static long num_transactions;
  private static double correlation;
  private static double corruption;
  private static int avg_transaction_size;
  private static int num_patterns;
  private static int avg_pattern_size;
  private static int num_items;

  private static String db_name;

  private static void showUsage()
  {
    System.out.println("gendb v1.0 (C)2002 Laurentiu Cristofor\n\na synthetic database generator\n\nUsage:\n\njava gendb <num_transactions> <avg_transaction_size> <num_patterns> <avg_pattern_size> <num_items> <correlation> <corruption>");
    System.exit(0);
  }

  public static void main(String args[])
  {
    if (args.length != 7)
      showUsage();

    try
      {
	num_transactions = Long.parseLong(args[0]);
	if (num_transactions < 1)
	  showUsage();
      }
    catch (NumberFormatException e)
      {
	showUsage();
      }

    try
      {
	avg_transaction_size = Integer.parseInt(args[1]);
	if (avg_transaction_size < 1)
	  showUsage();
      }
    catch (NumberFormatException e)
      {
	showUsage();
      }

    try
      {
	num_patterns = Integer.parseInt(args[2]);
	if (num_patterns < 1)
	  showUsage();
      }
    catch (NumberFormatException e)
      {
	showUsage();
      }

    try
      {
	avg_pattern_size = Integer.parseInt(args[3]);
	if (avg_pattern_size < 1)
	  showUsage();
      }
    catch (NumberFormatException e)
      {
	showUsage();
      }

    try
      {
	num_items = Integer.parseInt(args[4]);
	if (num_items < 1
	    || num_items < avg_transaction_size
	    || num_items < avg_pattern_size)
	  showUsage();
      }
    catch (NumberFormatException e)
      {
	showUsage();
      }

    try
    {
	correlation = Double.parseDouble(args[5]);
	if (correlation < 0 || correlation > 1)
		showUsage();
    } catch (NumberFormatException e)
      {
	showUsage();
	}
    try
    {
	corruption = Double.parseDouble(args[6]);
	if (corruption < 0 || corruption > 1)
		showUsage();
    } catch (NumberFormatException e)
      {
	showUsage();
	}

    db_name = "T" + num_transactions + "_AT" + avg_transaction_size
      + "_I" + num_items
      + "_P" + num_patterns + "_AP" + avg_pattern_size + ".db";

    genSynDB();
  }

  private static void genSynDB()
  {
    try
      {
	System.out.print("Initializing synthetic data generator...");
	SyntheticDataGenerator sdg 
	  = new SyntheticDataGenerator(num_transactions, avg_transaction_size,
				       num_patterns, avg_pattern_size,
				       num_items, correlation, corruption);
	System.out.println(" done!");
	
	// if file exists, delete it, otherwise DBWriter will append
	// to it
	File f = new File(db_name);
	if (f.exists())
	  f.delete();

	System.out.print("Generating synthetic database " + db_name + "...");
	DBWriter dbw = new DBWriter(db_name);
	ArrayList col_names = new ArrayList();
	for (int i = 0; i < num_items; i++)
	  col_names.add("C" + (i + 1));
	dbw.setColumnNames(col_names);
	dbw.setDescription(db_name);
	while (sdg.hasMoreTransactions())
	  dbw.addRow(sdg.getNextTransaction());
	dbw.close();
	System.out.println(" done!");
	System.exit(0);
      }
    catch (Exception e)
      {
	System.out.println("ERROR! " + e);
	System.exit(1);
      }
  }
}
