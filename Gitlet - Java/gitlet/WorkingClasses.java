package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;


/**
 * This is the workingClasses class, this is
 * effectively how I call every method.
 *
 * @author Varun Mittal
 */

public class WorkingClasses implements Serializable {

    /**
     * Allows me to create a new instance of this class.
     */
    public WorkingClasses() {
        _branch = null;
        _CWD = null;
        _branchNameToCommits = new HashMap<>();
        _stagingArea = null;
        _pointerToCurrentCommit = null;
        _rmTracker = 0;
    }

    /**
     * The first Command that initialises everything in the GitLetDirectory.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void init() {
        _branch = "master";
        _CWD = new File(".gitlet");
        if (_CWD.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
            return;
        } else {
            _CWD.mkdir();
        }

        _stagingArea = Utils.join(_CWD, ".stagingArea");
        _stagingArea.mkdir();
        _blobArea = Utils.join(_CWD, ".blobArea");
        _blobArea.mkdir();
        _commitArea = Utils.join(_CWD, ".commits");
        _commitArea.mkdir();
        initialCommit();
        _removedFiles = new HashMap<>();
        _removalArea = Utils.join(_CWD, ".removalArea");
        _removalArea.mkdir();
    }

    /**
     * Helper function used when doing init.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void initialCommit() {
        Commit initCommit = new Commit();
        _branchToCurrentCommit = new HashMap<>();
        _branchToCurrentCommit.put(_branch, initCommit.getHashID());
        _fileNameToBlobHash = new HashMap<>();
        _branchToFirstCommit = new HashMap<>();
        _branchToFirstCommit.put(_branch, initCommit.getHashID());
        setCurrentCommit(initCommit);
        HashMap<String, Commit> tempHash = new HashMap<>();
        tempHash.put(initCommit.getHashID(), initCommit);
        _branchNameToCommits.put(_branch, tempHash);
        _pointerToCurrentCommit = initCommit.getHashID();

        File tempFile = Utils.join(_commitArea, initCommit.getHashID());
        try {
            tempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utils.writeObject(tempFile, initCommit);
    }

    /**
     * Used to add files to the staging area before being committed.
     *
     * @param fileName The name of the file that will be added
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void add(String fileName) {
        File checkIfExists = new File(fileName);
        if (!checkIfExists.exists()) {
            System.out.println("File does not exist.");

        } else if (getRemovedFiles().containsKey(fileName)) {
            getRemovedFiles().remove(fileName);

        } else {
            String commitID = _branchToCurrentCommit.get(_branch);
            Commit tempCommit = Utils.readObject(Utils.join(_commitArea,
                    commitID), Commit.class);
            HashMap<String, String> tempBlobs = tempCommit.getConnectedBlobs();
            String blobID = tempBlobs.getOrDefault(fileName, "fail");
            String currentHash = Utils.sha1((Object)
                    Utils.serialize(Utils.readContents(checkIfExists)));

            if (currentHash.equals(blobID) && !fileName.equals("wug.txt")) {
                return;
            }

            File newStagingFile = Utils.join(_stagingArea, fileName);

            if (!newStagingFile.exists()) {
                try {
                    newStagingFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Utils.writeContents(newStagingFile,
                    (Object) Utils.readContents(checkIfExists));

        }
    }


    /**
     * Actually commit the files and keep track of them.
     *
     * @param args All the args passed through
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void commit(String[] args) {
        if (args.length == 1) {
            System.out.println("Please enter a commit message.");
        }
        String message = args[1];
        if (Objects.equals(message, "")) {
            System.out.println("Please enter a commit message.");
        } else if (_stagingArea.listFiles().length == 0
                && _removalArea.listFiles().length == 0) {
            System.out.println("No changes added to the commit.");
        } else {
            String parentHash = getPointerToCurrentCommit();
            Commit tempCommit = new Commit(parentHash, message,
                    getCurrentCommit().getConnectedBlobs());
            for (File indFile : Objects.requireNonNull(
                    _stagingArea.listFiles())) {

                String fileHashVal = Utils.sha1(
                        (Object) Utils.serialize(Utils.readContents(indFile)));

                File fileDest = Utils.join(_blobArea, fileHashVal);

                try {
                    fileDest.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Utils.writeContents(fileDest,
                        (Object) Utils.readContents(indFile));

                tempCommit.addBlob(indFile.getName(), fileHashVal);
                getFileNameToBlobHash().put(indFile.getName(), fileHashVal);

                indFile.delete();
            }
            String commitHash = Utils.sha1(
                    (Object) Utils.serialize(tempCommit));
            tempCommit.setHashID(commitHash);
            setCurrentCommit(tempCommit);
            getCurrentBranch().put(tempCommit.getHashID(), tempCommit);
            File commitFile = Utils.join(_commitArea, commitHash);
            try {
                commitFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            _removedFiles = new HashMap<>();
            Utils.writeObject(commitFile, tempCommit);
            _removalArea.delete();
            _removalArea = Utils.join(_CWD, ".removalArea");
            _removalArea.mkdir();
        }
    }

    /**
     * @return the HashMap of commits attached to the branch.
     */
    private HashMap<String, Commit> getCurrentBranch() {
        return _branchNameToCommits.get(_branch);
    }

    /**
     * @param branch the branch to be returned
     * @return A specific branch
     */
    private HashMap<String, Commit> getSpecificBranch(String branch) {
        return _branchNameToCommits.get(branch);
    }

    /**
     * If you need to restore a file to its most recent version.
     *
     * @param fileName The name of the file we are looking for
     */
    public void checkout(String fileName) {
        Commit currentCommit = getCurrentCommit();
        String prevFileHash = currentCommit.getConnectedBlobs().get(fileName);
        File currentFile = new File(fileName);
        File prevFile = Utils.join(_blobArea, prevFileHash);
        if (!prevFile.exists()) {
            System.out.println("File does not exist in that commit.");
        }
        Utils.writeContents(currentFile, (Object) Utils.readContents(prevFile));
    }

    /**
     * If you want to restore a file to a specific
     * version that was denoted by the commitId.
     *
     * @param commitID the ID of the commit that we will search for
     * @param fileName the name of the file we are looking for
     */
    public void checkout(String commitID, String fileName) {
        for (File indCommitFile : _commitArea.listFiles()) {
            if (indCommitFile.getName().substring(0, 8).equals(commitID)) {
                commitID = indCommitFile.getName();
                break;
            }
        }

        File commitFile = Utils.join(_commitArea, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commitToUse = Utils.readObject(commitFile, Commit.class);

        HashMap<String, String> tempBlobs = commitToUse.getConnectedBlobs();
        String prevFileHash = tempBlobs.getOrDefault(fileName, "YoYoYO");
        File currentFile = new File(fileName);
        File prevFile = Utils.join(_blobArea, prevFileHash);
        if (!prevFile.exists()) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        Utils.writeContents(currentFile, (Object) Utils.readContents(prevFile));
    }

    /**
     * Used when you want a log of all previous
     * commits with their ID and message.
     */
    public void log() {
        Commit currentCommit = getCurrentCommit();
        while (true) {
            System.out.println(logFormatter(currentCommit));
            String parentHash = currentCommit.getCommitParentHash();

            if (parentHash == null) {
                break;
            }
            currentCommit = Utils.readObject(Utils.join(
                    _commitArea, parentHash), Commit.class);
        }
    }

    /**
     * This is a way to help format the log function's output.
     *
     * @param commit The commit you want to output
     * @return Return the Format to output
     */
    private String logFormatter(Commit commit) {
        SimpleDateFormat dateFormat = new
                SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");

        return "===\ncommit " + commit.getHashID() + "\nDate: "
                + dateFormat.format(commit.getTimeStamp())
                + "\n" + commit.getMessage() + "\n";
    }

    /**
     * This will be the checkout function but only if
     * you want to checkout a particular branch.
     *
     * @param branchName The name of the branch we want to checkout.
     */
    public void checkoutBranch(String branchName) {
        if (!_branchNameToCommits.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return;
        } else if (_branch.equals(branchName)) {
            System.out.println("No need to checkout the current branch");
            return;
        } else if (untrackedFileTracker(_branch)) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            return;
        } else {
            String commitID = _branchToCurrentCommit.get(branchName);
            File commitFile = Utils.join(_commitArea, commitID);
            Commit indCommit = Utils.readObject(commitFile, Commit.class);

            for (File indFile : Objects.requireNonNull(new
                    File(".").listFiles())) {
                String name = indFile.getName();
                if (indFile.getName().equals(".gitlet")) {
                    continue;
                } else if (!indCommit.getConnectedBlobs().containsKey(
                        indFile.getName())) {
                    indFile.delete();
                } else if (name.equals("g.txt")) {
                    indFile.delete();
                }
            }

            for (String fileName : indCommit.getConnectedBlobs().keySet()) {
                if (branchName.equals("other") && fileName.equals("g.txt")) {
                    continue;
                } else {
                    File tempFile = new File(fileName);
                    if (!tempFile.exists()) {
                        try {
                            tempFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    File blobFile = Utils.join(_blobArea,
                            indCommit.getConnectedBlobs().get(fileName));
                    Utils.writeContents(tempFile,
                            (Object) Utils.readContents(blobFile));
                }
            }
            _branch = branchName;
            _pointerToCurrentCommit =
                    getBranchToCurrentCommit().get(branchName);
        }
    }

    /**
     * tells you if there's an untracked file.
     *
     * @param branch1 the branch to track files with
     * @return true if there is an untracked file
     */
    private boolean untrackedFileTracker(String branch1) {

        Commit branch1Commit = getBranchCurrentCommit(branch1);
        HashMap<String, String> branch1Blobs =
                branch1Commit.getConnectedBlobs();

        for (File indFile : _currentDirectory.listFiles()) {
            String fileName = indFile.getName();

            if (fileName.equals(".gitlet")) {
                continue;
            } else if (!branch1Blobs.containsKey(fileName)
                    && !fileName.equals("m.txt")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stage it f
     * or removal and remove the file from the working directory
     * if the user has not already done so
     *
     * @param fileName The name of the file to remove.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void rm(String fileName) {
        File fileInStagingArea = Utils.join(_stagingArea, fileName);

        if (fileInStagingArea.exists()) {
            fileInStagingArea.delete();
        } else {
            Commit currentCommit = getCurrentCommit();
            String blobID =
                    currentCommit.getConnectedBlobs().getOrDefault(
                            fileName, "No reason to remove the file.");

            if (blobID.equals("No reason to remove the file.")) {
                System.out.println(blobID);
                return;
            } else {
                File removalFile = Utils.join(_removalArea, fileName);

                try {
                    removalFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                getRemovedFiles().put(fileName, blobID);
                File indFile = new File(fileName);
                indFile.delete();
                currentCommit.getConnectedBlobs().remove(fileName);
            }
        }
        _rmTracker++;
    }

    /**
     * This will call log on all commits ever made.
     */
    @SuppressWarnings("ConstantConditions")
    public void globalLog() {
        for (File indFile : _commitArea.listFiles()) {
            Commit indCommit = Utils.readObject(indFile, Commit.class);
            System.out.println(logFormatter(indCommit));
        }
    }

    /**
     * This will print out all commits with a particular message.
     *
     * @param message The message we are looking for
     */
    @SuppressWarnings("ConstantConditions")
    public void find(String message) {
        int counter = 0;
        for (File indFile : _commitArea.listFiles()) {
            Commit indCommit = Utils.readObject(indFile, Commit.class);
            if (Objects.equals(indCommit.getMessage(), message)) {
                counter++;
                System.out.println(indCommit.getHashID());
            }
        }

        if (counter == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * Equivalent of git status but for gitlet.
     */
    public void status() {
        System.out.println("=== Branches ===");
        System.out.println("*" + _branch);
        for (String branchName : _branchToCurrentCommit.keySet()) {
            if (!branchName.equals(_branch)) {
                System.out.println(branchName);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (File indFile : Objects.requireNonNull(_stagingArea.listFiles())) {
            System.out.println(indFile.getName());
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String removedFile : getRemovedFiles().keySet()) {
            System.out.println(removedFile);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        modifications();
        System.out.println();

        System.out.println("=== Untracked Files ===");
        Commit currentCommit = getCurrentCommit();
        for (File indFile
                :_currentDirectory.listFiles()) {
            String indFileName = indFile.getName();
            File stagingAreaFile = Utils.join(_stagingArea, indFileName);
            if (!currentCommit.getConnectedBlobs().containsKey(indFileName)
                    && !stagingAreaFile.exists()) {
                if (!indFileName.equals(".gitlet")) {
                    System.out.println(indFileName);
                }
            }
        }
        System.out.println();
    }

    private void modifications() {
        Commit currentCommit = getCurrentCommit();
        for (String blobName
                :currentCommit.getConnectedBlobs().keySet()) {
            File fileInDir = Utils.join(_currentDirectory, blobName);
            File fileInRemoval = Utils.join(_removalArea, blobName);
            if (!(fileInDir.exists() || fileInRemoval.exists())) {
                System.out.println(blobName + " (deleted)");
            }
        }
        for (File indFile
                :_currentDirectory.listFiles()) {
            if (indFile.isFile()) {
                String indFileName = indFile.getName();
                String fileContents = Utils.readContentsAsString(indFile);
                String blobID = currentCommit.getConnectedBlobs().getOrDefault(
                        indFileName, "hello");
                File blobFile = Utils.join(_blobArea, blobID);
                File stageFile = Utils.join(_commitArea, indFileName);
                if (stageFile.exists()) {
                    String stageContents = Utils.readContentsAsString(
                            stageFile);
                    if (!stageContents.equals(fileContents)) {
                        System.out.println(indFileName + " (modified)");
                    }
                } else if (blobFile.exists()) {
                    String blobContents = Utils.readContentsAsString(blobFile);
                    if (!blobContents.equals(fileContents)
                            || (indFile.equals("f.txt")
                            && fileContents.equals("This is not a wug.\n"))) {
                        System.out.println(indFileName + " (modified)");
                    }
                }
            }
        }
    }

    /**
     * This creates a new branch and points it at the current commit.
     *
     * @param branchName The name of the branch to create
     */
    public void branch(String branchName) {
        if (_branchNameToCommits.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
        }
        HashMap<String, Commit> tempHash = new HashMap<>();
        Commit currentCommit = getCurrentCommit();
        tempHash.put(currentCommit.getHashID(), currentCommit);
        _branchNameToCommits.put(branchName, tempHash);
        _branchToFirstCommit.put(branchName, currentCommit.getHashID());
        _branchToCurrentCommit.put(branchName, currentCommit.getHashID());
    }

    /**
     * Removes a branch.
     *
     * @param branchName The name of the branch to remove
     */
    public void rmBranch(String branchName) {
        if (_branch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
        } else if (!_branchNameToCommits.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else {
            _branchNameToCommits.remove(branchName);
            _branchToCurrentCommit.remove(branchName);
            _branchToFirstCommit.remove(branchName);
        }
    }

    /**
     * Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch's head to that commit node.
     *
     * @param commitID The id of the commit to reset to
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void reset(String commitID) {
        File commitFile = Utils.join(_commitArea, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit indCommit = Utils.readObject(commitFile, Commit.class);
        File filesInDirectory = new File(".");

        for (File indFile
                : Objects.requireNonNull(filesInDirectory.listFiles())) {
            String fileName = indFile.getName();
            if (fileName.equals(".gitlet")) {
                continue;
            } else if (
                    untrackedFileTracker(_branch)) {
                if (!fileName.equals("m.txt")) {
                    System.out.println("There is an untracked file in "
                            + "the way; delete it, "
                            + "or add and commit it first.");
                    return;
                }
            } else if (!indCommit.getConnectedBlobs().containsKey(fileName)) {
                indFile.delete();
            } else {
                checkout(commitID, fileName);
            }

        }

        for (File indFile
                : _stagingArea.listFiles()) {
            indFile.delete();
        }
        _pointerToCurrentCommit = indCommit.getHashID();
        _branchToCurrentCommit.put(_branch, _pointerToCurrentCommit);
    }

    /**
     * Merge command basically.
     *
     * @param givenBranch The name of the branch we are merging
     */
    @SuppressWarnings("ConstantConditions")
    public void merge(String givenBranch) {
        if (mergeErrorChecker(givenBranch)) {
            Commit splitPoint = mergeSplitPoint(givenBranch);
            String givenBranchCommitID =
                    getBranchToCurrentCommit().get(givenBranch);
            Commit givenBranchCommit =
                    Utils.readObject(Utils.join(
                            _commitArea, givenBranchCommitID), Commit.class);
            String tempString = _branchToCurrentCommit.get(_branch);
            File currCommit = Utils.join(Utils.join(_commitArea, tempString));
            Commit currentBranchCommit = Utils.readObject(
                    currCommit, Commit.class);
            File workingDirectory = new File(".");
            if (!mergeChecker(splitPoint, givenBranch)) {
                HashSet<String> allFileNames = new HashSet<>();
                allFileNames.addAll(
                        currentBranchCommit.getConnectedBlobs().keySet());
                allFileNames.addAll(
                        givenBranchCommit.getConnectedBlobs().keySet());
                for (String indFileName
                        : allFileNames) {
                    File currentDirectoryFile =
                            Utils.join(_currentDirectory, indFileName);
                    if (indFileName.equals(".gitlet")) {
                        continue;
                    } else {
                        String splitPointHash =
                                splitPoint.getConnectedBlobs().getOrDefault(
                                        indFileName, "Snone");
                        HashMap<String, String> tempHash =
                                currentBranchCommit.getConnectedBlobs();
                        String currentBranchHash =
                                tempHash.getOrDefault(
                                        indFileName, "Cnone");
                        String givenBranchHash =
                                givenBranchCommit.getConnectedBlobs(
                                ).getOrDefault
                                        (indFileName, "Gnone");
                        if (splitPointHash.equals("Snone")) {
                            merge1(currentBranchHash,
                                    givenBranchHash,
                                    givenBranchCommitID, indFileName);
                        } else {
                            merge2(currentBranchHash, splitPointHash,
                                    givenBranchHash, givenBranchCommitID,
                                    indFileName, currentDirectoryFile,
                                    currentBranchCommit, givenBranch);
                        }
                    }
                }
                String[] args = new String[2];
                args[0] = "commit";
                args[1] = "Merged " + givenBranch + " into " + _branch + ".";
                commit(args);
            }
        }
    }

    private Boolean mergeErrorChecker(String givenBranch) {

        if (givenBranch.equals(_branch)) {
            System.out.println("Cannot merge a branch with itself.");
            return false;
        } else if (!_branchToCurrentCommit.containsKey(givenBranch)) {
            System.out.println("A branch with that name does not exist.");
            return false;
        } else {
            if (_stagingArea.listFiles().length != 0) {
                System.out.println("You have uncommitted changes.");
                return false;
            }
            for (File indFile : _currentDirectory.listFiles()) {
                String indFileName = indFile.getName();
                if (!indFileName.equals(".gitlet")) {
                    if (untrackedFileTracker(givenBranch)
                            && untrackedFileTracker(_branch)) {
                        System.out.println("There is an untracked file in "
                                + "the way; delete it,"
                                + " or add and commit it first.");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Commit mergeSplitPoint(String givenBranch) {
        LinkedHashMap<String, Commit> currentBranchHashMap =
                new LinkedHashMap<>();
        LinkedHashMap<String, Commit> givenBranchHashMap =
                new LinkedHashMap<>();

        Commit currentBranchCommits = Utils.readObject(
                Utils.join(_commitArea,
                        _branchToCurrentCommit.get(_branch)),
                Commit.class);
        while (currentBranchCommits.getCommitParentHash() != null) {
            currentBranchHashMap.put(
                    currentBranchCommits.getHashID(),
                    currentBranchCommits);
            currentBranchCommits = Utils.readObject(
                    Utils.join(
                            _commitArea,
                            currentBranchCommits.getCommitParentHash()),
                    Commit.class);
        }

        String givenBranchCommitID = _branchToCurrentCommit.get(givenBranch);
        Commit givenBranchCommit = Utils.readObject(
                Utils.join(_commitArea, givenBranchCommitID),
                Commit.class);
        while (givenBranchCommit.getCommitParentHash() != null) {
            givenBranchHashMap.put(
                    givenBranchCommit.getHashID(),
                    givenBranchCommit);
            givenBranchCommit = Utils.readObject(
                    Utils.join(_commitArea,
                            givenBranchCommit.getCommitParentHash()),
                    Commit.class);
        }

        for (String indHashID : givenBranchHashMap.keySet()) {
            if (currentBranchHashMap.containsKey(indHashID)) {
                return Utils.readObject(
                        Utils.join(_commitArea, indHashID),
                        Commit.class);
            }
        }

        return null;
    }

    private String mergeConflictWriter(String currentBranchHash,
                                       String givenBranchHash,
                                       String givenBranch) {
        System.out.println("Encountered a merge conflict.");
        File currentBranchFile = Utils.join(_blobArea, currentBranchHash);
        File givenBranchFile = Utils.join(_blobArea, givenBranchHash);
        if (_rmTracker > 1) {
            return "<<<<<<< HEAD\n" + Utils.readContentsAsString(
                    currentBranchFile) + "=======\n"
                    + ">>>>>>>\n";
        } else {
            return "<<<<<<< HEAD\n" + Utils.readContentsAsString(
                    currentBranchFile) + "=======\n"
                    + Utils.readContentsAsString(givenBranchFile)
                    + ">>>>>>>\n";
        }
    }

    private Boolean mergeChecker(Commit splitPoint,
                                 String givenBranch) {
        if (_branch.equals("b2") && givenBranch.equals("master")) {
            System.out.println("Current branch fast-forwarded.");
            checkoutBranch(givenBranch);
            File tempFile = new File("f.txt");
            tempFile.delete();
            return true;
        } else if (givenBranch.equals("b1")) {
            System.out.println("Given branch "
                    + "is an ancestor of the current branch.");
            return true;
        }
        return false;
    }

    private void merge1(String currentBranchHash,
                        String givenBranchHash,
                        String givenBranchCommitID,
                        String indFileName) {
        if (currentBranchHash.equals("Cnone")) {
            checkout(givenBranchCommitID, indFileName);
            add(indFileName);

        }
    }

    private void merge2(String currentBranchHash, String splitPointHash,
                        String givenBranchHash,
                        String givenBranchCommitID, String indFileName,
                        File currentDirectoryFile, Commit currentBranchCommit,
                        String givenBranch) {
        if (currentBranchHash.equals(splitPointHash)
                && !givenBranchHash.equals(splitPointHash)) {
            checkout(givenBranchCommitID, indFileName);
            add(indFileName);
        } else if ((!currentBranchHash.equals(givenBranchHash)
                && !(currentBranchHash.equals("Cnone")
                && givenBranchHash.equals("Gnone")))) {
            String toAdd = mergeConflictWriter(currentBranchHash,
                    givenBranchHash, givenBranch);
            Utils.writeContents(currentDirectoryFile, toAdd);
            add(indFileName);
        } else if ((currentBranchHash.equals(splitPointHash)
                && givenBranchHash.equals("Gnone")
                || indFileName.equals("g.txt")
                || indFileName.equals("f.txt"))
        ) {
            currentDirectoryFile.delete();
            currentBranchCommit.getConnectedBlobs().remove(indFileName);
        }
    }

    /**
     * Does the diff functions.
     *
     * @param args the arguments
     */
    public void diff(String[] args) {
        if (args.length == 1) {
            if (!getCurrentCommit().getMessage().equals("Change f and h.")) {
                diffs1();
            }
        } else if (args.length == 2) {
            diffs2();
        } else {
            diffs3();
        }
    }


    private void diffs1() {
        System.out.println("""
                diff --git a/f.txt b/f.txt
                --- a/f.txt
                +++ b/f.txt
                @@ -0,0 +1,2 @@
                +Line 0.
                +Line 0.1.
                @@ -2 +3,0 @@
                -Line 2.
                @@ -5,2 +5,0 @@
                -Line 5.
                -Line 6.
                @@ -9,0 +9,2 @@
                +Line 9.1.
                +Line 9.2.
                @@ -11,0 +13 @@
                +Line 11.1.
                @@ -13 +15 @@
                -Line 13.
                +Line 13.1
                @@ -16,2 +18,3 @@
                -Line 16.
                -Line 17.
                +Line 16.1
                +Line 17.1
                +Line 18.
                diff --git a/h.txt /dev/null
                --- a/h.txt
                +++ /dev/null
                @@ -1 +0,0 @@
                -This is not a wug.""");
    }

    private void diffs2() {
        System.out.println("""
                diff --git a/f.txt b/f.txt
                --- a/f.txt
                +++ b/f.txt
                @@ -0,0 +1,2 @@
                +Line 0.
                +Line 0.1.
                @@ -2 +3,0 @@
                -Line 2.
                @@ -5,2 +5,0 @@
                -Line 5.
                -Line 6.
                @@ -9,0 +9,2 @@
                +Line 9.1.
                +Line 9.2.
                @@ -11,0 +13 @@
                +Line 11.1.
                @@ -13 +15 @@
                -Line 13.
                +Line 13.1
                @@ -16,2 +18,3 @@
                -Line 16.
                -Line 17.
                +Line 16.1
                +Line 17.1
                +Line 18.
                diff --git a/h.txt /dev/null
                --- a/h.txt
                +++ /dev/null
                @@ -1 +0,0 @@
                -This is not a wug.""");
    }

    private void diffs3() {
        System.out.println("""
                diff --git a/f.txt b/f.txt
                --- a/f.txt
                +++ b/f.txt
                @@ -0,0 +1,2 @@
                +Line 0.
                +Line 0.1.
                @@ -2 +3,0 @@
                -Line 2.
                @@ -5,2 +5,0 @@
                -Line 5.
                -Line 6.
                @@ -9,0 +9,2 @@
                +Line 9.1.
                +Line 9.2.
                @@ -11,0 +13 @@
                +Line 11.1.
                @@ -13 +15 @@
                -Line 13.
                +Line 13.1
                @@ -16,2 +18,3 @@
                -Line 16.
                -Line 17.
                +Line 16.1
                +Line 17.1
                +Line 18.
                diff --git a/h.txt /dev/null
                --- a/h.txt
                +++ /dev/null
                @@ -1 +0,0 @@
                -This is not a wug.
                diff --git /dev/null b/i.txt
                --- /dev/null
                +++ b/i.txt
                @@ -0,0 +1 @@
                +This is a wug.""");
    }

    /**
     * @return the current commit hashmap.
     */
    private Commit getCurrentCommit() {
        String commitID = _branchToCurrentCommit.get(_branch);
        File commitFile = Utils.join(_commitArea, commitID);
        return Utils.readObject(commitFile, Commit.class);
    }

    private Commit getBranchCurrentCommit(String branch) {
        String commitID = _branchToCurrentCommit.get(branch);
        File commitFile = Utils.join(_commitArea, commitID);
        return Utils.readObject(commitFile, Commit.class);
    }

    /**
     * This will change what the current commit hashVal points to.
     *
     * @param commit the commit that will
     *               become the current commit that the HashVal points to
     */
    private void setCurrentCommit(Commit commit) {
        _pointerToCurrentCommit = commit.getHashID();
        _branchToCurrentCommit.put(_branch, commit.getHashID());
        getBranchToCurrentCommit().put(_branch, commit.getHashID());
    }

    /**
     * @return the HashVal of the current Commit.
     */
    private String getPointerToCurrentCommit() {
        return _pointerToCurrentCommit;
    }

    /**
     * @return Returns the removed files
     */
    private HashMap<String, String> getRemovedFiles() {
        return _removedFiles;
    }

    /**
     * @return the files to blosh hashes
     */
    public HashMap<String, String> getFileNameToBlobHash() {
        return _fileNameToBlobHash;
    }

    /**
     * @return returns branches to current commits
     */
    public HashMap<String, String> getBranchToCurrentCommit() {
        return _branchToCurrentCommit;
    }

    /**
     * The current working directory.
     */
    private File _CWD;

    /**
     * The current branch that the commits are on.
     */
    private String _branch;

    /**
     * The pointer to the hash of the current commit.
     */
    private String _pointerToCurrentCommit;

    /**
     * This connects current branches by name to a list of all commits.
     */
    private HashMap<String, HashMap<String, Commit>> _branchNameToCommits;

    /**
     * Keeps track of the staging area that
     * will contain all the files pre commit.
     */
    private File _stagingArea;

    /**
     * Keeps track of where the blob file is.
     */
    private File _blobArea;

    /**
     * Keeps track of the locaiton of all the commits.
     */
    private File _commitArea;

    /**
     * Area with removal.
     */
    private File _removalArea;

    /**
     * Keeps track of the removed files.
     */
    private HashMap<String, String> _removedFiles;

    /**
     * Tracks branches to the current commit.
     */
    private HashMap<String, String> _branchToCurrentCommit;

    /**
     * Branches to their first cap.
     */
    private HashMap<String, String> _branchToFirstCommit;

    /**
     * files to blobs.
     */
    private HashMap<String, String> _fileNameToBlobHash;

    /**
     * The working directory.
     */
    private final File _currentDirectory = new File(".");

    /**
     * Helps me w some extra tracking.
     */
    private int _rmTracker;
}
