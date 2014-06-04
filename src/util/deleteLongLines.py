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
    if len(sys.argv) != 3: 
        errorExit("Usage: {} MAXLEN FILE\n".format(os.path.basename(sys.argv[0])))
    maxlen = int(sys.argv[1])
    fileName = sys.argv[2]
    if not os.path.isfile(fileName):
        errorExit("{} does not exist, or is not a file\n".format(fileName))
    deletedLines = 0
    with (open(fileName, 'rt')) as FILE:
            for line in FILE:
                items = line.split()
                if len(items) <= maxlen:
                    sys.stdout.write(line)
                else:
                    deletedLines += 1
    sys.stderr.write("{}: {} lines deleted from {}\n".format(sys.argv[0],
        deletedLines, fileName))
    return 0

if __name__ == "__main__":
    main()

