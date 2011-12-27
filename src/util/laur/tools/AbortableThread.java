/*
  AbortableThread.java
   
  (P)2001-2002 Laurentiu Cristofor
*/

/*

laur.tools - A Java package containing general purpose classes
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


The laur.tools package was written by Laurentiu Cristofor (laur@cs.umb.edu).

*/

package laur.tools;

/**

   This abstract class offers a general mechanism for thread abortion.

   @version 1.0
   @author Laurentiu Cristofor
   
*/
public abstract class AbortableThread extends Thread
{
  /**
   * This will be true if the user requested abort, false otherwise.
   */
  protected boolean bAbort;

  /**
   * This will be true if the thread is running, false otherwise.
   */
  protected boolean isRunning;

  /**
   * This method should be overwritten by subclasses to perform the
   * thread actions.
   */
  protected abstract void execute();

  /**
   * Runs thread. The thread can be interrupted through a call to abort().
   *
   * @exception IllegalStateException   if the thread is still running
   */
  public void run()
  {
    if (isRunning)
      throw new IllegalStateException();

    try
      {
	isRunning = true;

	execute();
      }
    catch (AbortThreadRequest abort)
      {
	// reset bAbort and terminate
	bAbort = false;
      }
    catch (OutOfMemoryError e)
      {
	System.err.println("OUT OF MEMORY! : " + e);
      }
    finally
      {
	isRunning = false;

	terminationHook();
      }
  }

  /**
   * Called before thread terminates execution. By default does
   * nothing, overwrite to provide specific behavior.
   */
  protected void terminationHook()
  {
  }

  /**
   * Requests the abortion of the thread.
   */
  public void abort()
  {
    // do nothing if already stopped
    if (!isRunning)
      return;

    bAbort = true;
  }

  /**
   * This method should be called in subclasses at critical points to
   * check whether the user requested that the execution be aborted.
   * If the user wants to abort, then this method throws an 
   * AbortThreadRequest exception. AbortableThread.run() catches this
   * exception.
   */
  protected void checkAbort()
  {
    if (bAbort)
      throw new AbortThreadRequest();
  }
}
