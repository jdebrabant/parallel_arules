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

    basefileName = os.path.basename(fileName)
    print("'{0}': {{'size': {1}, ''vcdimupbound': {2}, maxlen': {3},  'numitems': {4}}}".format(basefileName, size, VCDimUpBound, maxLen, len(items)))

if __name__ == '__main__':
    main()

