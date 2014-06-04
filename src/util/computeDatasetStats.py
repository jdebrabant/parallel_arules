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
def errorExit(msg):
    sys.stderr.write(msg)
    sys.exit(1)

def main():
    # Verify arguments
    if len(sys.argv) != 2:
        errorExit("Usage: {} FILE\n".format(os.path.basename(sys.argv[0])))
    fileName = sys.argv[1]
    if not os.path.isfile(fileName):
        errorExit("{} does not exist, or is not a file\n".format(fileName))

    size = 0
    maxLen = 0
    items = set() 
    # The following dict will contain, for each length, the number of
    # transactions of that length.
    numTransLengthDict = dict()
    with open(fileName) as FILE:
        for line in FILE:
            size = size + 1
            lineItemsList = line.split()
            if len(lineItemsList) in numTransLengthDict:
                numTransLengthDict[len(lineItemsList)] += 1
            else:
                numTransLengthDict[len(lineItemsList)] = 1
            if len(lineItemsList) > maxLen:
                maxLen = len(lineItemsList)
            for item in lineItemsList:
                items.add(item)

    longer = 0
    for length in sorted(numTransLengthDict.keys(), reverse=True):
        if longer + numTransLengthDict[length] >= length:
            VCDimUpBound = length
            break
        longer += numTransLengthDict[length]

    basefileName = os.path.basename(fileName)
    print("'{0}': {{'size': {1}, ''vcdimupbound': {2}, maxlen': {3},  'numitems': {4}}}".format(basefileName, size, VCDimUpBound, maxLen, len(items)))

if __name__ == '__main__':
    main()

