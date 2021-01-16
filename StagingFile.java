package gitlet;

import java.io.File;
import java.nio.file.FileStore;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StagingFile {

    private static final File STAGING_FILE = new File(".gitlet/staging.txt");

    private List<Line> lines;

    public static class Line {
        private StagingFileStatus status;

        private String sha;

        private String fileName;

        private Line(StagingFileStatus status, String sha, String fileName) {
            this.status = status;
            this.sha = sha;
            this.fileName = fileName;
        }

        private Line(String line) {
            String parts[] = line.split(" ");
            this.status = StagingFileStatus.valueOf(parts[0]);
            this.sha = parts[1];
            this.fileName = parts[2];
        }

        public String toLine() {
            return status + " " + sha + " " + fileName;
        }

        public StagingFileStatus getStatus() {
            return status;
        }

        public String getSha() {
            return sha;
        }

        public String getFileName() {
            return fileName;
        }

        public boolean isFileChanged() {
            byte[] content = Utils.readContents(new File(fileName));
            String sha = Utils.sha1(content);
            return !this.sha.equals(sha);
        }
    }

    private StagingFile(List<Line> lines) {
        this.lines = lines;
    }

    public List<Line> getLines() {
        return lines;
    }

    public static StagingFile readFromFile() {
        List<Line> lines = new ArrayList<>();
        StagingFile sf = new StagingFile(lines);
        if (!STAGING_FILE.exists())
            return sf;
        String statingContent = Utils.readContentsAsString(STAGING_FILE);
        if (statingContent == null)
            return sf;
        for (String line : statingContent.split("\n")) {
            if (line == null || line.isBlank())
                continue;
            lines.add(new Line(line));
        }
        lines.sort(new Comparator<Line>() {
            @Override
            public int compare(Line line1, Line line2) {
                return line1.fileName.compareTo(line2.fileName);
            }
        });
        return sf;
    }

    public void updateStatusForCommit() {
        List<Line> newLines = new ArrayList<>();
        for (Line line : lines) {
            if (line.status == StagingFileStatus.TRACKING || line.status == StagingFileStatus.COMMITTED) {
                newLines.add(line);
            }
        }
        this.lines = newLines;
    }

    public void storeToFile() {
        File stagingFile = new File(".gitlet/staging.txt");
        StringBuilder content = new StringBuilder();
        for (Line line : lines) {
            content.append(line.toLine());
            content.append("\n");
        }
        Utils.writeContents(STAGING_FILE, content.toString());
    }

    public Line findBySha(String sha) {
        for (Line line : lines) {
            if (sha.equals(line.sha)) {
                return line;
            }
        }
        return null;
    }

    public boolean removeByFilename(String fileName) {
        for (Line line : lines) {
            if (fileName.equals(line.fileName)) {
                line.status = StagingFileStatus.REMOVED;
                return true;
            }
        }
        return false;
    }

    public List<Line> findByStatus(StagingFileStatus ...statuses) {
        List<Line> retList = new ArrayList<>();
        for (Line line : lines) {
            for (StagingFileStatus s : statuses) {
                if (s.equals(line.status)) {
                    retList.add(line);
                    break;
                }
            }
        }
        return retList;
    }

    public void addByFilename(String fileName, String sha) {
        for (Line line : lines) {
            if (fileName.equals(line.fileName)) {
                // zaten varsa STAGING olarak update ediyoruz
                line.status = StagingFileStatus.TRACKING;
                line.sha = sha;
                return;
            }
        }
        lines.add(new Line(StagingFileStatus.TRACKING, sha, fileName));
    }

    /*
Sample:
junk.txt (deleted)
wug3.txt (modified)
     file in the working directory is "modified but not staged" if it is
    Tracked in the current commit, changed in the working directory, but not staged; or
    Staged for addition, but with different contents than in the working directory; or
    Staged for addition, but deleted in the working directory; or
    Not staged for removal, but tracked in the current commit and deleted from the working directory.
     */
    public List<Line> findModifications() {
        List<Line> retList = new ArrayList<>();
        for (Line line : lines) {
            if (StagingFileStatus.TRACKING == line.status || StagingFileStatus.COMMITTED == line.status) {
                File file = new File(line.fileName);
                if (!file.exists()) {
                    retList.add(new Line(StagingFileStatus.DELETED, line.sha, line.fileName));
                } else {
                    if (line.isFileChanged()) {
                        retList.add(new Line(StagingFileStatus.MODIFIED, line.sha, line.fileName));
                    }
                }
            }
        }
        return retList;
    }

    public List<String> findAllFilenames() {
        List<String> retList = new ArrayList<>();
        for (Line line : lines) {
            retList.add(line.fileName);
        }
        return retList;
    }
}
