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
            tokens = line.split("}")
            itemset = tokens[0][1:-1]
            frequency = float((tokens[1].split(" "))[0][2:-3])
            results.append((itemset + "\t" + str(frequency)+"\n", frequency))

    results.sort(key=itemgetter(1), reverse=True)

    for tup in results:
        sys.stdout.write(tup[0])

if __name__ == "__main__":
    main()

