import os, sys
from operator import itemgetter

def errorExit(msg):
    sys.stderr.write(msg)
    sys.exit(1)

def main():
    # Verify arguments
    if len(sys.argv) != 3:
        errorExit("Usage: {} MINSUP FILE\n".format(os.path.basename(sys.argv[0])))
    minSup = int(sys.argv[1])
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
        if tup[1] >= minSup:
            itemsetStr = ""
            for item in sorted(tup[0]):
                itemsetStr += str(item) + " " 
            itemsetStr = itemsetStr[:-1]
            sys.stdout.write(itemsetStr + "\t" + str(tup[1]) + "\n")
        else:
            break

if __name__ == '__main__':
    main()

