import os, sys
from operator import itemgetter

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

    results = []
    with open(fileName) as FILE:
        for line in FILE:
            tokens = line.split("(")
            itemsStr = tokens[0].split()
            items = []
            for itemStr in itemsStr:
                items.append(int(itemStr))
            itemsetStr = ""
            for item in sorted(items):
                itemsetStr += str(item) + " "
            sortedLine = itemsetStr + "(" + tokens[1]
            tokens2 = tokens[1].split(",")
            freq = float(tokens2[0])
            results.append((sortedLine, freq))
    results.sort(key=itemgetter(1), reverse=True)

    for tup in results:
        sys.stdout.write(tup[0])

if __name__ == "__main__":
    main()

