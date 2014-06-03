import os, sys
from operator import itemgetter

def errorExit(msg):
    sys.stderr.write(msg)
    sys.exit(1)

def main():
    # Verify arguments
    if len(sys.argv) != 3: 
        errorExit("Usage: {} SIZE FILE\n".format(os.path.basename(sys.argv[0])))
    dsSize = int(sys.argv[1])
    fileName = sys.argv[2]

    if not os.path.isfile(fileName):
        errorExit("{} does not exist, or is not a file\n".format(fileName))

    results = []
    with open(fileName) as FILE:
        for line in FILE:
            if "(" in line:
                tokens = line.split("(")
                itemsStr = tokens[0].split()
                items = []
                for itemStr in itemsStr:
                    item = str(itemStr[1:])
                    items.append(item)
                itemsetStr = ""
                for item in sorted(items):
                    itemsetStr += str(item) + " "
                frequency = int(tokens[1][:-2]) / dsSize
                results.append((itemsetStr + "\t" + str(frequency)+"\n", frequency))

    results.sort(key=itemgetter(1), reverse=True)

    for tup in results:
        sys.stdout.write(tup[0])

if __name__ == "__main__":
    main()

