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

