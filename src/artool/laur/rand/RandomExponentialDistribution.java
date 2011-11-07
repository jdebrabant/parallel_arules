/*
  RandomExponentialDistribution.java

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
 
   A generator of random numbers with exponential distribution.

   @version 1.0
   @author Laurentiu Cristofor
 
 */
public class RandomExponentialDistribution
{
  private Random rand;
  private double mean;

  /**
   * Create a new generator of random numbers
   * with exponential distribution of mean 1.
   */
  public RandomExponentialDistribution()
  {
    this(1.0, new Random());
  }

  /**
   * Create a new generator of random numbers
   * with exponential distribution of specified mean.
   *
   * @param mean   the mean of the exponential distribution
   */
  public RandomExponentialDistribution(double mean)
  {
    this(mean, new Random());
  }

  /**
   * Create a new generator of random numbers with exponential
   * distribution of specified mean that will use <code>randgen</code>
   * as its source of random numbers.
   *
   * @param mean   the mean of the exponential distribution
   * @param randgen   a Random object to be used by the generator.
   */
  public RandomExponentialDistribution(double mean, Random randgen)
  {
    this.mean = mean;
    rand = randgen;
  }

  /**
   * Return a random number with exponential distribution.
   */
  public double nextDouble()
  {
    double val;

    do
      val = rand.nextDouble();
    while (val == 0.0);

    return mean * (-Math.log(val));
  }

  /**
   * sample usage and testing
   */
  public static void main(String[] args)
  {
    RandomExponentialDistribution exponential 
      = new RandomExponentialDistribution(10);

    for (int i = 0; i <= 10; i++)
      System.out.println(exponential.nextDouble());
  }
}
