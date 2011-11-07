/*
  dbtool.java
 
  (P)2000-2001 Laurentiu Cristofor
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

/**
 * This program performs various operations on a database.
 *
 * @version 	1.0
 * @author	Laurentiu Cristofor
 */
import java.util.ArrayList;

public class dbtool
{
  private static final int ACT_DISPLAY_INFO         = 0;
  private static final int ACT_DISPLAY_DESCRIPTION  = 1;
  private static final int ACT_CHECK_INTEGRITY      = 10;
  private static final int ACT_SET_DESCRIPTION      = 20;

  private static int action;
  private static boolean hasParameter;
  private static String parameter;
  private static String dbName;

  private static void showUsage()
  {
    System.out.println("dbtool v1.0 (C)2002 Laurentiu Cristofor\n\nThis program provides several useful database operations.\n\nUsage:\n\njava dbtool <operation> [parameter] <dbfile>\n\nwhere <operation> is one of:\n-di\tdisplay database information\n-dd\tdisplay database description\n-c\tcheck database integrity\n-sd\tset database description to string passed as parameter");
    System.exit(0);
  }

  public static void main(String args[])
  {
    if (args.length < 2 || args.length > 3)
      showUsage();

    if (args[0].equals("-di"))
      action = ACT_DISPLAY_INFO;
    else if (args[0].equals("-dd"))
      action = ACT_DISPLAY_DESCRIPTION;
    else if (args[0].equals("-c"))
      action = ACT_CHECK_INTEGRITY;
    else if (args[0].equals("-sd"))
      {
	action = ACT_SET_DESCRIPTION;
	hasParameter = true;
	parameter = args[1];
      }
    else
      showUsage();

    if (hasParameter && args.length != 3
	|| !hasParameter && args.length > 2)
      showUsage();

    if (hasParameter)
      dbName = args[2] + ".db";
    else
      dbName = args[1] + ".db";

    performAction(dbName);
  }

  private static void performAction(String dbName)
  {
    try
      {
	DBReader dbr = new DBReader(dbName);
	DBWriter dbw = new DBWriter(dbName);

	switch (action)
	  {
	  case ACT_DISPLAY_INFO:
	    System.out.println("\nDATABASE INFORMATION:");
	    System.out.println("\nversion: " + dbr.getVersion());
	    System.out.println("\ndescription: " + dbr.getDescription());
	    System.out.println("\nrows (" + dbr.getNumRows() + ")");
	    System.out.println("\ncolumns (" + dbr.getNumColumns() + ") :");
	    ArrayList col_names = dbr.getColumnNames();
	    for(int i = 0; i < col_names.size(); i++)
	      System.out.println("\t" + (i + 1) + " " + col_names.get(i));
	    break;

	  case ACT_DISPLAY_DESCRIPTION:
	    System.out.println("\ndescription: " + dbr.getDescription());
	    break;

	  case ACT_CHECK_INTEGRITY:
	    System.out.print("Checking integrity of database... ");
	    System.out.println(dbr.checkIntegrity() ? "passed!" : "failed!");
	    break;

	  case ACT_SET_DESCRIPTION:
	    dbw.setDescription(parameter);
	    break;
	  }
	

	dbr.close();
	dbw.close();
      }
    catch (Exception e)
      {
	System.out.println("ERROR! " + e);
	//e.printStackTrace();
      }
  }
}
