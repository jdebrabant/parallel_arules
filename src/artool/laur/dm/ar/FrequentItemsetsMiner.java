/*
  FrequentItemsetsMiner.java
   
  (P)1999-2002 Laurentiu Cristofor
*/

/*

laur.dm.ar - A Java package for association rule mining 
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


The laur.dm.ar package was written by Laurentiu Cristofor (laur@cs.umb.edu).

*/

package laur.dm.ar;

import laur.tools.*;

/**

   This abstract class must be extended by the algorithms that will look
   for large itemsets.
   
   @version 1.0
   @author Laurentiu Cristofor

*/
public abstract class FrequentItemsetsMiner extends MonitoredThread
{
  /**
   * With this object we read from the database
   */
  protected DBReader db_reader;

  /**
   * With this object we write to the cache.
   */
  protected DBCacheWriter cache_writer;

  /**
   * Minimum support value.
   */
  protected double min_support;

  // this will keep the mining result (the number of passes)
  private int result;


  /**
   * Find the frequent itemsets in a database
   *
   * @param dbReader   the object used to read from the database
   * @param cacheWriter   the object used to write to the cache
   * if this is null, then nothing will be saved, this is useful
   * for benchmarking
   * @param minSupport   the minimum support
   * @return   the number of passes executed over the database
   */
  public abstract int findFrequentItemsets(DBReader dbReader, 
					   DBCacheWriter cacheWriter,
					   double minSupport);


  /**
   * Sets the parameters for the mining algorithm. This method should
   * be used before starting the thread.
   *
   * @param monitor   an object that we should notify about important
   * events 
   * @param dbReader   the object used to read from the database
   * @param cacheWriter   the object used to write to the cache
   * if this is null, then nothing will be saved, this is useful
   * for benchmarking
   * @param minSupport   the minimum support
   * @exception IllegalStateException   if the thread is already running
   */
  public void setParameters(ThreadMonitor monitor,
			    DBReader dbReader, 
			    DBCacheWriter cacheWriter,
			    double minSupport)
  {
    if (isRunning)
      throw new IllegalStateException();

    this.monitor = monitor;

    db_reader = dbReader;
    cache_writer = cacheWriter;
    min_support = minSupport;
  }

  /**
   * Executes the findFrequentItemsets() method.
   */
  protected void execute()
  {
    result = findFrequentItemsets(db_reader, cache_writer, min_support);
  }

  /**
   * Gets the value returned by findFrequentItemsets() after the thread
   * completed its execution.
   *
   * @return   the number of passes executed over the database
   * @exception IllegalStateException   if the thread is still running
   * or if no result is available
   */
  public int getResult()
  {
    if (isRunning)
      throw new IllegalStateException("thread still running");

    if (result == 0)
      throw new IllegalStateException("thread was never run or it was aborted");

    return result;
  }
}
