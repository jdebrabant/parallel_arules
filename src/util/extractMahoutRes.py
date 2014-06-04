"""
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
"""

import os, sys
from operator import itemgetter

def errorExit(msg):
    sys.stderr.write(msg)
    sys.exit(1)

def main():
    # Verify arguments
    if len(sys.argv) != 3:
        errorExit("Usage: {} DSSIZE FILE\n".format(os.path.basename(sys.argv[0])))
    dsSize = int(sys.argv[1])
    fileName = sys.argv[2]
    if not os.path.isfile(fileName):
        errorExit("{} does not exist, or is not a file\n".format(fileName))
    results = dict([])
    with (open(fileName, 'rt')) as FILE:
        for line in FILE:
            tokens = line.split("[")
            for token in tokens[1:]:
                tokens2 = token.split("]")
                itemsetStr = tokens2[0]
                items = []
                for item in itemsetStr.split(","):
                    items.append(int(item))
                itemset = frozenset(items)
                supportStr = (tokens2[1].split(","))[1][:-1]
                # Handle end of the line
                if supportStr[-1] == ")":
                    supportStr = supportStr[:-1]
                support = int(supportStr)
                results[itemset] = support

    sortedResults = sorted(results.items(), key=itemgetter(1), reverse=True)

    for tup in sortedResults:
        itemsetStr = ""
        for item in sorted(tup[0]):
            itemsetStr += str(item) + " " 
        itemsetStr = itemsetStr[:-1]
        sys.stdout.write(itemsetStr + "\t" + str(tup[1] / dsSize) + "\n")

if __name__ == '__main__':
    main()

