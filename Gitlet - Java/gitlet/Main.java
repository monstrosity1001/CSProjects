package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Varun Mittal
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String[] args) {
        File metaData = new File(".gitlet/.metaData");
        File gitletDir = new File(".gitlet");
        WorkingClasses executableMethods = new WorkingClasses();

        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (args[0].equals("status") && !(gitletDir.exists())) {
            System.out.println("Not in an initialized Gitlet directory.");
        } else if (Objects.equals(args[0], "init")) {
            executableMethods.init();

            try {
                metaData.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.writeObject(metaData, executableMethods);

        } else {

            executableMethods =
                    Utils.readObject(metaData, WorkingClasses.class);

            if (Objects.equals(args[0], "checkout")) {
                if (args.length == 2) {
                    executableMethods.checkoutBranch(args[1]);
                } else if (!Objects.equals(args[1], "--")) {
                    if (args[2].equals("++")) {
                        System.out.println("incorrect operands");
                    } else {
                        executableMethods.checkout(args[1], args[3]);
                    }
                } else {
                    executableMethods.checkout(args[2]);
                }

            } else {

                switch (args[0]) {

                case "add" -> executableMethods.add(args[1]);
                case "commit" -> executableMethods.commit(args);
                case "log" -> executableMethods.log();
                case "rm" -> executableMethods.rm(args[1]);
                case "global-log" -> executableMethods.globalLog();
                case "find" -> executableMethods.find(args[1]);
                case "status" -> executableMethods.status();
                case "branch" -> executableMethods.branch(args[1]);
                case "rm-branch" -> executableMethods.rmBranch(args[1]);
                case "reset" -> executableMethods.reset(args[1]);
                case "merge" -> executableMethods.merge(args[1]);
                case "diff" -> executableMethods.diff(args);
                default -> System.out.println("No command"
                        + " with that name exists.");
                }
            }
            Utils.writeObject(metaData, executableMethods);
        }
    }
}
