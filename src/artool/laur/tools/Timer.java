/*
  Timer.java

  (P)2000-2001 Laurentiu Cristofor
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
 * This class implements a timer. The timer can be in one of two states:
 * started or stopped. Method start() makes a transition from stopped
 * to started, and method stop() does a transition from started to stopped.
 * Method time() can be used to get the total time measured by the timer
 * and it can be called when the timer is in either of the two states.
 * Method reset() will reset the timer; if the timer was running then it 
 * will continue to run but it will measure time from the moment we called
 * reset(), if the timer was stopped, the time measured will be reset to 0.
 *
 * @version 1.0
 * @author Laurentiu Cristofor
 */
public class Timer
{
  private long measured_time; // the time we measured so far
  private boolean started;    // is the timer running?
  private long start_time;    // the time when we started/restarted 
                              // the timer

  /**
   * Initialize a new timer.
   */
  public Timer()
  {
    measured_time = 0;
    started       = false;
  }

  /**
   * This method should be called to start/restart the timer. 
   * If the timer was already started then this won't do 
   * anything, otherwise the timer will be started.
   */
  public void start()
  {
    if (started)
      return;

    started = true;
    start_time = System.currentTimeMillis();
  }

  /**
   * This method should be called to stop/pause the timer.
   * If the timer wasn't started then this won't do anything,
   * otherwise the timer will be stoped.
   */
  public void stop()
  {
    if (!started)
      return;

    long stop_time = System.currentTimeMillis();
    started = false;
    // add the time we measured up to now
    measured_time += stop_time - start_time;
  }

  /**
   * This method should be called to reset the timer. If the timer
   * was started then it will continue running but it will measure
   * the time starting from now. If the timer wasn't started then
   * the method will just reset the time measured so far to 0.
   */
  public void reset()
  {
    measured_time = 0;

    if (started)
      start_time = System.currentTimeMillis();
  }

  /**
   * This method should be called to find out the time measured so
   * far.  If the timer is started then the time measured up to this
   * precise moment will be returned. If the timer was stopped then
   * the time measured up to the time it was stopped will be
   * returned. The time is measured in milliseconds.
   *
   * @return the measured time in milliseconds
   */
  public long time()
  {
    if (started)
      {
	long current_time = System.currentTimeMillis();
	return measured_time + (current_time - start_time); 
      }
    else
      return measured_time;
  }

  /**
   * This method should be called to find out if the timer is
   * started or not.
   *
   * @return true if timer is started, false otherwise
   */
  public boolean isStarted()
  {
    return started;
  }

  /**
   * This method returns a string representation of our timer.
   */
  public String toString()
  {
    return "Time measured so far: " + time() + " (ms)";
  }
}
