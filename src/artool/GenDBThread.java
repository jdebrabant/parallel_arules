/*
  GenDBThread.java
   
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

import javax.swing.*;
import java.util.*;

/**

   This class implements a thread module for generatng a synthetic
   database using class SyntheticDataGenerator.
   
   @version 1.0
   @author Laurentiu Cristofor

*/
public class GenDBThread extends MonitoredThread
{
  private SyntheticDataGenerator sdg;
  private DBWriter dbw;
  private JProgressBar jpb;

  /**
   * Here we construct and initialize the thread.
   */
  public GenDBThread(ThreadMonitor monitor,
		     SyntheticDataGenerator sdg,
		     DBWriter dbw,
		     JProgressBar jpb)
		     
  {
    this.monitor = monitor;
    this.sdg = sdg;
    this.dbw = dbw;
    this.jpb = jpb;
  }

  /**
   * Here we generate the synthetic database.
   */
  protected void execute()
  {
    try
      {
	int count = 0;
	jpb.setValue(count);

	while (sdg.hasMoreTransactions())
	  {
	    dbw.addRow(sdg.getNextTransaction());
	    count++;
	    jpb.setValue(count);
	    checkAbort();
	  }

	jpb.setValue(0);
      }
    catch (AbortThreadRequest abort)
      {
	jpb.setValue(0);
	throw abort;
      }
    catch (Throwable e)
      {
	Workspace.getWorkspace().log(Strings.UNXP_ERROR
				     + "GenDBThread.execute(): " 
				     + e + Strings.endl);
      }
  }
}
