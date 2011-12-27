/*
  RandomSample.java

  (P)2000-2001 Laurentiu Cristofor
*/

/*

laur.rand - A Java package for random number generators and other
random methods
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


The laur.rand package was written by Laurentiu Cristofor (laur@cs.umb.edu).

*/

package laur.rand;

import java.util.Random;

/**
 
   This class allows the random selection of n numbers from 1 to N.<P>
 
   The algorithm used here is described in Knuth, TAOCP, volume 2,
   second print, section 3.4.2, algorithm S on page 122 (page 142 in
   the third edition)

   @version 1.0
   @author Laurentiu Cristofor
 
 */
public class RandomSample
{
  // I use the variable naming from Knuth !
  private long N;
  private int n;
  private Random rand;

  /**
   * Create a new generator of random samples of <code>n</code> distinct 
   * values from the numbers 1 to <code>N</code>.
   *
   * @param N   specifies the range from which we want a sample, 
   * the range will be 1...<code>N</code>.
   * @param n   specifies the size of the sample, the sample will contain
   * distinct values in the range specified by <code>N</code>.
   * @exception IllegalArgumentException   thrown if <code>n</code> or
   * <code>N</code> are smaller than 1 or if <code>n</code> is greater
   * than <code>N</code>.
   */
  public RandomSample(long N, int n)
  {
    this(N, n, new Random());
  }

  /**
   * Create a new generator of random samples of <code>n</code>
   * distinct values from the numbers 1 to <code>N</code> using
   * <code>randgen</code> as a source of random numbers.
   *
   * @param N   specifies the range from which we want a sample, 
   * the range will be 1...<code>N</code>.
   * @param n   specifies the size of the sample, the sample will contain
   * distinct values in the range specified by <code>N</code>.
   * @param randgen   a Random object to be used by the generator.
   * @exception IllegalArgumentException   thrown if <code>n</code> or
   * <code>N</code> are smaller than 1 or if <code>n</code> is greater
   * than <code>N</code>.
   */
  public RandomSample(long N, int n, Random randgen)
  {
    if (N < 1 || n < 1 || N < n)
      throw new IllegalArgumentException("You must provide n and N s.t. 0 < n <= N");

    this.N = N;
    this.n = n;
    rand = randgen;
  }

  /**
   * Return a random sample.
   *
   * @return   an array of <code>n</code> numbers sampled from the
   * numbers 1...<code>N</code>.
   */
  public long[] nextSample()
  {
    long[] sample = new long[n];
    int i = 0;

    // S1. [Initialize]
    long t = 0; // how many we have seen
    int m = 0; // how many we have selected
    while (true)
      {
	// S2. [Generate U]
	double U = rand.nextDouble();
	// S3. [Test]
	if ((N - t) * U >= n - m)
	  {
	    // S5. [Skip]
	    t = t + 1;
	  }
	else
	  {
	    // S4. [Select]
	    sample[i++] = t + 1;
	    m = m + 1;
	    t = t + 1;

	    if (m < n)
	      ;
	    else // sample complete !
	      return sample;
	  }
      }
  }

  /**
   * sample usage and testing
   */
  public static void main(String[] args)
  {
    RandomSample rs = new RandomSample(100, 7);

    for (int i = 0; i < 10; i++)
      {
	long[] sample = rs.nextSample();
	for (int j = 0; j < sample.length; j++)
	  System.out.print(sample[j] + " ");
	System.out.println("");
      }
  }
}
