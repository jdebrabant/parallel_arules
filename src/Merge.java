/*
 * Copyright 2012-14 Justin A. Debrabant <debrabant@cs.brown.edu> and Matteo Riondato <matteo@cs.brown.edu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

class Pair
{
	String set;
	int support;
	Pair(String inset, int insupport)
	{
		set = inset;
		support = insupport;
	}
}
public class Merge {
	/*
	 *Read the file, parse all the lines and sort them according to the item set.
	 **/
	public static Vector<Pair> parse (String filename) throws NumberFormatException, IOException
	{
		FileInputStream fstream = new FileInputStream(filename);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine, set;
		String[] part;
		int support, i, j;
		Vector<Pair> result = new Vector<Pair>();
		
		while((strLine = br.readLine()) != null)
		{
			part = strLine.split(" ");
			//get itemset string
			set = "";
			for(j = 0; j < part.length - 1; j++)
			{
				set = set.concat(part[j] + " ");
			}
			//get support number
			support = Integer.valueOf(part[part.length - 1].trim());
			
			//insert into the result list, sorts them at the same time
			for(i = 0; i < result.size(); i++)
			{
				if(compare(set, result.get(i).set) < 0)
				{
					result.add(i, new Pair(set, support));
					break;
				}
				else if(compare(set, result.get(i).set) == 0)
				{
					System.out.println(set + " " + support + "!!!");
					result.set(i, new Pair(set, support + result.get(i).support));
					break;
				}
			}
			if(i == result.size())
				result.add(new Pair(set, support));
		}
		return result;
	}
	
	//merge the results of two files
	public static Vector<Pair> merge (Vector<Pair> v1, Vector<Pair> v2)
	{
		int i = 0, j = 0, c;
		Vector<Pair> result = new Vector<Pair>();
		while(true)
		{
			c = compare(v1.get(i).set, v2.get(j).set);
			if(c == 0)
			{	
				Pair newPair = new Pair(v1.get(i).set, v1.get(i).support + v2.get(j).support);
				result.add(newPair);
				i++;
				j++;
			}
			else if(c < 0)
			{
				result.add(v1.get(i));
				i++;
			}
			else
			{
				result.add(v2.get(j));
				j++;
			}
			if(i == v1.size() || j == v2.size())
				break;
		}
		if(i < v1.size())
		{
			while(i < v1.size())
			{
				result.add(v1.get(i));
				i++;
			}
		}
		
		if(j < v2.size())
		{
			while(j < v2.size())
			{
				result.add(v2.get(j));
				j++;
			}
		}
		return result;
	}
	/*
	 * Compare two item sets
	 * */
	public static int compare(String s1, String s2)
	{
		if(s1.equals(s2))
			return 0;
		else
		{
			String part1[] = s1.split(" ");
			String part2[] = s2.split(" ");
			int i = 0;
			while(true)
			{
				int c = Integer.valueOf(part1[i]) - Integer.valueOf(part2[i]);
				if(c != 0)
					return c;
				else
					i++;
				if(i == part1.length)
					return -1;
				else if(i == part2.length)
					return 1;
			}
		}
	}
	public static void main(String[] args) throws IOException
	{
		Vector<Pair> file0 = Merge.parse("part-r-00000"); 
		Vector<Pair> file1 = Merge.parse("part-r-00001"); 
		Vector<Pair> file2 = Merge.parse("part-r-00002"); 
		Vector<Pair> file3 = Merge.parse("part-r-00003"); 
		Vector<Pair> file5 = Merge.parse("part-r-00005"); 
		Vector<Pair> result01 = Merge.merge(file0, file1);
		Vector<Pair> result23 = Merge.merge(file2, file3);
		Vector<Pair> result0123 = Merge.merge(result01, result23);
		Vector<Pair> result = Merge.merge(result0123, file5);
		FileWriter fstream = new FileWriter("result");
		BufferedWriter out = new BufferedWriter(fstream);
		for(int i = 0; i < result.size(); i++)
		{
			out.write(result.get(i).set + "=" + result.get(i).support+ "\n");
		}
		out.close();
	}

}
