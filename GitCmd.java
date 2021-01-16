package gitlet;

public enum GitCmd {
    INIT("init", 0),
    ADD("add", 1),
    COMMIT("commit", 1),
    REMOVE("rm", 1),
    LOG("log", 0),
    STATUS("status", 0);

    private String cmd;

    private int argCount;

    GitCmd(String cmd, int argCount) {
        this.cmd = cmd;
        this.argCount = argCount;
    }

    public int getArgCount() {
        return argCount;
    }

    public String getCmd() {
        return cmd;
    }

    public static GitCmd fromCmd(String cmd) {
        for (GitCmd gitCmd : GitCmd.values()) {
            if (cmd.equals(gitCmd.cmd))
                return gitCmd;
        }
        return null;
    }

    public boolean isInitCommand() {
        return "init".equals(cmd);
    }

    public boolean isAddCommand() {
        return "add".equals(cmd);
    }

    public boolean isCommitCommand() {
        return "commit".equals(cmd);
    }

    public boolean isRemove() {
        return "rm".equals(cmd);
    }

    public boolean isLog() {
        return "log".equals(cmd);
    }

    public boolean isStatus() {
        return "status".equals(cmd);
    }
}
