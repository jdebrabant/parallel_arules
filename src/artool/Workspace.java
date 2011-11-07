/*
  Workspace.java
 
  (P)2002 Laurentiu Cristofor
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
import laur.tools.*;

import javax.swing.JTextArea;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The ARtool workspace - Singleton pattern
 *
 * @version 	1.1
 * @author	Laurentiu Cristofor
 */
public class Workspace
{
  // constants
  // status type
  public static final int IDLE        = 0;
  public static final int EXEC_ALG_IS = 1;
  public static final int EXEC_ALG_AR = 2;
  public static final int EXEC_SOME   = 3;

  // measure types
  public static final int M_CONFIDENCE = 0;
  public static final int M_PIATETSKY  = 1;
  public static final int M_LIFT       = 2;
  public static final int M_INFLUENCE  = 3;

  // THE workspace
  private static Workspace theWorkspace;

  // where we log messages
  public JTextArea log;

  // timer for measuring time spent by algorithms 
  public Timer timer;

  // database info
  public String db_name;
  public long db_size;
  public DBReader dbr;
  public ArrayList item_names;
  public HashMap name2item;

  // cache info
  public String cache_name;
  public DBCacheWriter dbcw;
  public DBCacheReader dbcr;

  // algorithm info
  public String algIS_type;
  public FrequentItemsetsMiner algIS;
  public String algAR_type;
  public AssociationsMiner algAR;

  // algorithm parameters
  public double minsup;
  public double minconf;
  public int max_antc_size;
  public int min_cons_size;
  public Itemset is_in_antc;
  public Itemset is_in_cons;
  public Itemset is_to_ignore;

  // these vectors keep the contents of the lists in the advanced
  // options pane
  public ArrayList v_items;
  public ArrayList v_in_antc;
  public ArrayList v_in_cons;
  public ArrayList v_to_ignore;

  // results
  public ArrayList itemsets;
  public ArrayList rules;

  // current rule measure
  public int measure_id;

  // keeps track of application status
  public int status;

  // keeps track of abort requests so that we don't send more than one
  // (one request is enough)
  public boolean bAbortRequested;


  // private constructor to ensure it's a Singleton
  private Workspace()
  {
    timer = new Timer();
  }

  // return a reference to the only instance of Workspace
  public static Workspace getWorkspace()
  {
    if (theWorkspace == null)
      theWorkspace = new Workspace();

    return theWorkspace;
  }

  // log a message
  public void log(String s)
  {
    log.append(s);
  }

  public void clearLog()
  {
    log.setText(Strings.empty);
  }

  // dump all workspace variables
  public void dump()
  {
    log.append(Strings.endl + Strings.DUMP + Strings.endl);
    log.append(Strings.empty + timer + Strings.endl);
    log.append(Strings.empty + db_name + Strings.endl);
    log.append(Strings.empty + db_size + Strings.endl);
    log.append(Strings.empty + dbr + Strings.endl);
    log.append(Strings.empty + item_names + Strings.endl);
    log.append(Strings.empty + name2item + Strings.endl);
    log.append(Strings.empty + cache_name + Strings.endl);
    log.append(Strings.empty + dbcw + Strings.endl);
    log.append(Strings.empty + dbcr + Strings.endl);
    log.append(Strings.empty + algIS_type + Strings.endl);
    log.append(Strings.empty + algIS + Strings.endl);
    log.append(Strings.empty + algAR_type + Strings.endl);
    log.append(Strings.empty + algAR + Strings.endl);
    log.append(Strings.empty + minsup + Strings.endl);
    log.append(Strings.empty + minconf + Strings.endl);
    log.append(Strings.empty + max_antc_size + Strings.endl);
    log.append(Strings.empty + min_cons_size + Strings.endl);
    log.append(Strings.empty + is_in_antc + Strings.endl);
    log.append(Strings.empty + is_in_cons + Strings.endl);
    log.append(Strings.empty + is_to_ignore + Strings.endl);
    log.append(Strings.empty + v_items + Strings.endl);
    log.append(Strings.empty + v_in_antc + Strings.endl);
    log.append(Strings.empty + v_in_cons + Strings.endl);
    log.append(Strings.empty + v_to_ignore + Strings.endl);
    log.append(Strings.empty + itemsets + Strings.endl);
    log.append(Strings.empty + rules + Strings.endl);
    log.append(Strings.empty + measure_id + Strings.endl);
    log.append(Strings.empty + status + Strings.endl);
    log.append(Strings.empty + bAbortRequested + Strings.endl);
    log.append(Strings.endl);
  }

  // clean up workspace, call this before exiting or whenever you need
  // to start with a fresh workspace
  public void cleanUp()
  {
    try
      {
	db_name = null;
	cache_name = null;
	item_names = null;
	name2item = null;
	itemsets = new ArrayList(0);
	rules = new ArrayList(0);

	max_antc_size = 0;
	min_cons_size = 0;
	is_in_antc = null;
	is_in_cons = null;
	is_to_ignore = null;

	if (dbr != null)
	  {
	    dbr.close();
	    dbr = null;
	  }
      }
    catch (IOException e)
      {
	log(Strings.UNXP_ERROR + "cleanUp(): " 
	    + e + Strings.endl);
      }
  }
}
