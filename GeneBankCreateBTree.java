import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import java.io.PrintStream;

public class GeneBankCreateBTree {
	private static int cacheSelect, degree, seqLength, debugLevel = 0;
	private static String file, fileDataName;
	private static boolean debug = false;
	public static void main(String[] args) {
		try {
			if (args.length < 3 || args.length > 5) {
				printUsage();
				System.exit(-1);
			}
			if (args.length == 5) {
				debug = true;
				debugLevel = Integer.parseInt(args[4]);
			}
			cacheSelect = Integer.parseInt(args[0]);
			if (!(cacheSelect == 0 || cacheSelect == 1)) {
				printUsage();
				System.exit(-1);
			}
			degree = Integer.parseInt(args[1]);
				if (degree == 0)
				{
					degree = 15; //optimal degree
				}
			file = args[2];
			seqLength = Integer.parseInt(args[3]);
			fileDataName = file + ".btree.data." + seqLength + "." + degree;
			if (seqLength > 31 || seqLength < 1) {
				System.err.println("Sequence Size must be between 1 and 31 inclusive.");
				printUsage();
				System.exit(-1);
			}
		} catch (Exception e) {
			printUsage();
			System.exit(-1);
		}
		if (!debug || debugLevel == 0) {
			BTree tree = new BTree(degree, fileDataName, seqLength);
      try {
        Parse(args[2],seqLength,tree);
        tree.writeTreeData();
      } catch(FileNotFoundException e) {
        e.printStackTrace();
      }
		} else if (debugLevel == 1) {
			BTree tree = new BTree(degree, fileDataName, seqLength);
      try {
        Parse(args[2],seqLength,tree);
        tree.writeTreeData();
      } catch(FileNotFoundException e) {
        e.printStackTrace();
      }
			PrintStream dumpFile = null;
			try {
				String str = "dump";
				dumpFile = new PrintStream(new FileOutputStream(str));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			System.setOut(dumpFile);
			tree.inOrderPrint(tree.getRoot());
			System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		}

	}

  //When everything is all set up, this throws all the keys into the tree
  //BTree tree = new BTree(Integer.parseInt(args[0]), args[1]+".btree.data."+args[2]+"."+args[0], Integer.parseInt(args[2]));
  //Parse(args[1],Integer.parseInt(args[2]),tree);
  //tree.writeTreeData();

private static void printUsage() {
        System.err.println("Usage: java GeneBankCreateBTree <cache> <degree> <gbk file> <sequence length> [<debuglevel>]");
        System.err.println("<cache>: use  (0 for default)");
        System.err.println("<degree>: degree of the BTree (0 for default)");
        System.err.println("<gbk file>: GeneBank file");
        System.err.println("<sequence length>: 1-31");
        System.err.println("[<debug level>]: 0/1 (no/yes)");
        System.exit(1);
    }
    private static void Parse(String fileName, int length, BTree destination) throws FileNotFoundException {
          //Open file in a scanner
          int total = 0;
          int lineNo = 1;
          File fi = new File(fileName);
          Scanner scan = new Scanner(fi);
          boolean toggle = false;
          Pattern p = Pattern.compile("(?i)(?=([actg]{"+length+"}))");
					String prevChars = "";
          while(scan.hasNextLine()) {
            lineNo++;
            String line = scan.nextLine().trim();
            if(!toggle) {
              if(line.equals("ORIGIN")) {
                toggle = true;
              }
          } else {
            if(line.equals("//")) {
              toggle = false;
              continue;
            }
            line = line.replaceAll("[\\s0-9]*","");
						//prevChars is the previous length-1 bits from the last line, it allows for line wrapping
            Matcher m = p.matcher(prevChars+line);
            while(m.find()) {
              total++;

            destination.insert(toLong(m.group(1)));
          }
					prevChars = line.substring(line.length() - length + 1,line.length());
          }

          }
          System.out.println(total+" total matches.");
    }
    private static Long toLong(String code) {
      String s = code.toLowerCase();
      s = s.replaceAll("a","00");
      s = s.replaceAll("t","11");
      s = s.replaceAll("c","01");
      s = s.replaceAll("g","10");
      Long m = 1l;
      m = m<<63; //One with 63 zeroes after it, so we can always have 64 bits
      return (Long.parseLong(s,2) | m); //Mask it so if we ever wanted to see the full binary value we can
    }
}
