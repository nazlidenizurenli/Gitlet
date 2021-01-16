package gitlet;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Nazli Urenli
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        try {
            CmdLineParser parser = new CmdLineParser(args);
            parser.validate();
            Gitlet gitlet = new Gitlet(parser);
            gitlet.process();
        } catch (GitletException ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
        }
    }



/*
    public void log() {
        //    // Commit'in headini al
        while (myCommit != null) {
            System.out.println("===");
            System.out.println("commit " + myCommit.getHashCode());
            System.out.println("Date:" + myCommit.getDate());
//
            if (myCommit.getParent() == null) {
                System.out.println("initial commit");
            } else if () {
                System.out.println("Another commit message.");
            } else {
                System.out.println("A commit message.");
            }
            // Recursive olarak parentlara ayni islemi yap
            myCommit = myCommit.getParent();
        }
    }
*/
    //private static void rm(String[] args) {
    //  // File yoksa remove edilemez.
    //  if (args.length != 2) {
    //      System.out.println("Please input the file to remove");
    //      return;
    //  }
    //}

}
