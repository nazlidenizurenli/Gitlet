package gitlet;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Gitlet {

    private CmdLineParser parser;

    public Gitlet(CmdLineParser parser) {
        this.parser = parser;
    }

    public void process() {
        String cmd = parser.getCommand();
        GitCmd gitCmd = GitCmd.fromCmd(cmd);
        if (gitCmd.isInitCommand()) {
            initCommand();
        } else if (gitCmd.isAddCommand()) {
            addCommand(parser.getParam1());
        } else if (gitCmd.isRemove()) {
            removeCommand(parser.getParam1());
        } else if (gitCmd.isCommitCommand()) {
            commitCommand(parser.getParam1(), false);
        } else if (gitCmd.isStatus()) {
            statusCommand();
        }
    }

    private void statusCommand() {
        StagingFile stagingFile = StagingFile.readFromFile();
        System.out.println("=== Branches ===");
        // TODO: Branch ler yazilacak

        System.out.println("");
        System.out.println("=== Staged Files ===");
        for (StagingFile.Line line : stagingFile.findByStatus(StagingFileStatus.TRACKING)) {
            if (!line.isFileChanged()) {
                System.out.println(line.getFileName());
            }
        }

        System.out.println("");
        System.out.println("=== Removed Files ===");
        for (StagingFile.Line line : stagingFile.findByStatus(StagingFileStatus.REMOVED)) {
            System.out.println(line.getFileName());
        }

        System.out.println("");
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (StagingFile.Line line : stagingFile.findModifications()) {
            System.out.print(line.getFileName());
            if (line.getStatus() == StagingFileStatus.REMOVED) {
                System.out.println(" (deleted)");
            } else {
                System.out.println(" (modified)");
            }
        }

        System.out.println("");
        System.out.println("=== Untracked Files ===");
        List<String> filesInCurrentDir = Utils.plainFilenamesIn(".");
        List<String> allStagingFileNames = stagingFile.findAllFilenames();
        Collections.sort(filesInCurrentDir);
        for (String fileName : filesInCurrentDir) {
            if (!fileName.startsWith(".")
                && !allStagingFileNames.contains(fileName)) {
                System.out.println(fileName);
            }
        }
    }

    private void removeCommand(String param1) {
       StagingFile stagingFile = StagingFile.readFromFile();
       boolean success = stagingFile.removeByFilename(param1);
       if (!success)
           throw new GitletException("No reason to remove the file.");
        stagingFile.storeToFile();
    }


    private  void addCommand(String filename) {
        File f = new File(filename);
        if (!f.exists())
            throw new GitletException("File does not exist.");

        byte[] content = Utils.readContents(f);
        String sha = Utils.sha1(content);

        StagingFile stagingFile = StagingFile.readFromFile();
        StagingFile.Line line = stagingFile.findBySha(sha);
        if (line != null)  {
            // File hic degismemis tekrar eklemeyelim ama removed ise
            if (line.getStatus() != StagingFileStatus.REMOVED) {
                return;
            }
        }
        stagingFile.addByFilename(filename, sha);
        stagingFile.storeToFile();

        File shaFileName = Utils.join(".gitlet/blobs", sha);
        Utils.writeContents(shaFileName, content);

    }

    private  File findStagingDir() {
        String content = Utils.readContentsAsString(new File(".gitlet/HEAD.txt"));
        String sha = content.split("\n")[0];
        File stagingDir = new File(".gitlet/HEAD/" + sha);
        return stagingDir;
    }

    private void initCommand() {
        File f = new File(".gitlet");
        f.mkdir();
        new File(".gitlet/blobs").mkdir();
        new File(".gitlet/trees").mkdir();
        new File(".gitlet/commits").mkdir();
        Utils.writeContents(new File(".gitlet/staging.txt"), "");
        commitCommand("initial commit", true);

    }

    private  void commitCommand(String message, boolean initialCommit) {
        File headFile = new File(".gitlet/HEAD.txt");

        StagingFile stagingFile = StagingFile.readFromFile();
        List<StagingFile.Line> trackingFiles = stagingFile.findByStatus(StagingFileStatus.TRACKING);

        Date dt = null;
        StringBuilder fileContent = new StringBuilder();
        if (initialCommit) {
            fileContent.append("parent: \n");
            try {
                dt = ((new SimpleDateFormat("dd-M-yyyy hh:mm:ss"))).parse("01-01-1970 00:00:00");
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }
        } else {
            if (trackingFiles.isEmpty())
                throw new GitletException("No changes added to the commit.");

            dt = new Date();
            fileContent.append("parent: " + Utils.readContentsAsString(headFile) + "\n");
        }

        fileContent.append("Date: " + dt + "\n");
        fileContent.append(message + "\n");
        for (StagingFile.Line line : stagingFile.findByStatus(StagingFileStatus.TRACKING, StagingFileStatus.COMMITTED)) {
            fileContent.append(line.toLine());
            fileContent.append("\n");
        }
        stagingFile.updateStatusForCommit();
        stagingFile.storeToFile();

        String sha = Utils.sha1(fileContent.toString());
        File commitFile = Utils.join(".gitlet/commits", sha);
        Utils.writeContents(commitFile, fileContent.toString());
        Utils.writeContents(headFile, sha);
    }
}
