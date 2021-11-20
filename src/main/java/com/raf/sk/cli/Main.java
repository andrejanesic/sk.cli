package com.raf.sk.cli;

import com.raf.sk.core.actions.*;
import com.raf.sk.core.core.Core;
import com.raf.sk.core.exceptions.*;
import com.raf.sk.core.repository.Directory;
import com.raf.sk.core.repository.File;
import com.raf.sk.core.repository.INode;
import com.raf.sk.core.repository.INodeType;
import com.raf.sk.core.repository.limitations.INodeLimitation;
import com.raf.sk.core.repository.limitations.INodeLimitationType;
import com.raf.sk.core.user.IPrivilege;
import com.raf.sk.core.user.IUser;

import java.util.*;

/**
 * Klasa za glavni tok programa.
 */
public class Main {

    /**
     * Glavni tok programa.
     *
     * @param argv
     */
    public static void main(String[] argv) {
        DriverTypes driver = DriverTypes.LOCAL;
        IActionManager am = Core.getInstance().ActionManager();

        // parsiraj ulazne argumente
        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("--driver")) {
                if (i + 1 < argv.length) {
                    if (argv[i + 1].equals("local")) {
                        driver = DriverTypes.LOCAL;
                    } else if (argv[i + 1].equals("remote")) {
                        driver = DriverTypes.GOOGLE;
                    } else {
                        driver = null;
                    }
                }
            }
        }

        if (driver == null) {
            System.out.println("Error in driver selection, driver cannot be null!");
            System.exit(0);
        }

        try {
            if (driver == DriverTypes.LOCAL) {
                // #TODO zašto je ovo u default paketu?
                Class.forName("com.raf.sk.localImplementation.LocalImplementation");
            } else {
                Class.forName("com.raf.sk.remoteImplementation.RemoteImplementation");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }

        System.out.print("> ");
        Scanner sc = new Scanner(System.in);
        String inp;

        while (sc.hasNext()) {
            inp = sc.nextLine();
            if (inp == null || inp.length() == 0) {
                System.out.println("Bad input, please try again.");
                continue;
            }

            String[] args = inp.split(" ");
            if (args.length == 0) {
                System.out.println("Bad input, please try again.");
                continue;
            }

            try {
                switch (args[0]) {
                    case "init": {
                        IAction initConfig;
                        if (args.length < 3) {
                            System.out.println("These arguments are required: \"username\", \"password\".");
                            break;
                        }

                        // prvo, učitaj config
                        String cwd = System.getProperty("user.dir") + "\\config.json";
                        initConfig = new ActionInitConfig(cwd);
                        am.addAction(initConfig);
                        am.run();
                        System.out.println("[1/3] Config loaded...");

                        // potom, učitaj korisnika
                        IAction initUser;
                        initUser = new ActionInitUser(args[1], args[2]);
                        am.addAction(initUser);
                        am.run();
                        System.out.println("[2/3] User loaded...");

                        // konačno, učitaj storage
                        IAction initStorage;
                        initStorage = new ActionInitStorage();
                        am.addAction(initStorage);
                        am.run();
                        System.out.println("[3/3] Tree loaded...");
                        System.out.println("Storage ready!");
                        break;
                    }

                    /* --- USER --- */
                    case "login": {
                        if (args.length < 3) {
                            System.out.println("These arguments are required: \"username\", \"password\".");
                            break;
                        }
                        IAction initUser = new ActionInitUser(args[1], args[2]);
                        am.addAction(initUser);
                        am.run();
                        System.out.println("User logged in.");
                        break;
                    }

                    case "register": {
                        if (args.length < 3) {
                            System.out.println("These arguments are required: \"username\", \"password\".");
                            break;
                        }
                        IAction addUser = new ActionAddUser(args[1], args[2]);
                        am.addAction(addUser);
                        am.run();
                        System.out.println("User \"" + args[1] + "\" added.");
                        break;
                    }

                    case "deregister": {
                        if (args.length < 2) {
                            System.out.println("These arguments are required: \"username\".");
                            break;
                        }
                        IAction removeUser = new ActionDeleteUser(args[1]);
                        am.addAction(removeUser);
                        am.run();
                        System.out.println("User \"" + args[1] + "\" removed.");
                        break;
                    }

                    case "logout": {
                        IAction deinitUser = new ActionDeinitUser();
                        am.addAction(deinitUser);
                        am.run();
                        System.out.println("User logged out.");
                        break;
                    }

                    case "users": {
                        IAction getUsers = new ActionGetUsers();
                        //noinspection unchecked
                        Collection<IUser> users = (Collection<IUser>) getUsers.run();
                        if (users == null) {
                            throw new RuntimeException("Couldn't retrieve users.");
                        }
                        for (IUser u : Core.getInstance().UserManager().getUsers()) {
                            if (u.getUsername() != null && u.getUsername().length() > 0) {
                                System.out.println("<" +
                                        u.getUsername() +
                                        (u.isAuthenticated() ? "*" : "") +
                                        ">"
                                );
                                for (IPrivilege p : u.getPrivileges()) {
                                    System.out.print("\t+ " + p.getType());
                                    if (p.getReferencedObject() != null) {
                                        System.out.print(": " + p.getReferencedObject());
                                    }
                                    System.out.print("\n");
                                }
                            }
                        }
                        break;
                    }

                    /* --- PRIVILEGES --- */
                    case "grant": {
                        if (args.length < 3) {
                            System.out.println("These arguments are required: \"username\", \"privilegeType\".");
                            break;
                        }

                        IAction grantPrivilege;
                        if (args.length == 3) {
                            grantPrivilege = new ActionGrantPrivilege(args[1], args[2]);
                        } else {
                            grantPrivilege = new ActionGrantPrivilege(args[1], args[2], args[3]);
                        }
                        am.addAction(grantPrivilege);
                        am.run();
                        System.out.println("Privilege granted.");
                        break;
                    }

                    case "revoke": {
                        if (args.length < 3) {
                            System.out.println("These arguments are required: \"username\", \"privilegeType\".");
                            break;
                        }

                        IAction revokePrivilege;
                        if (args.length == 3) {
                            revokePrivilege = new ActionRevokePrivilege(args[1], args[2]);
                        } else {
                            revokePrivilege = new ActionRevokePrivilege(args[1], args[2], args[3]);
                        }
                        am.addAction(revokePrivilege);
                        am.run();
                        System.out.println("Privilege revoked.");
                        break;
                    }

                    /* --- STORAGE --- */
                    case "cd": {
                        if (args.length < 2) {
                            System.out.println("These arguments are required: \"path\".");
                            break;
                        }
                        IAction actionChangeCwd = new ActionChangeCwd(args[1]);
                        am.addAction(actionChangeCwd);
                        am.run();
                        break;
                    }

                    case "ls":
                    case "dir": {
                        IAction actionListDirectory;
                        if (args.length < 2)
                            actionListDirectory = new ActionListDirectory(".");
                        else
                            actionListDirectory = new ActionListDirectory(args[1]);

                        boolean showFiles = false, showDirs = false, recursive = false;
                        int sortMode = 1;
                        if (args.length == 1 || args.length == 2) {
                            showFiles = showDirs = true;
                        }
                        for (int i = 2; i < args.length; i++) {
                            String t = args[i];
                            if (t.equals("--files")) showFiles = true;
                            if (t.equals("--dirs")) showDirs = true;
                            if (t.equals("--rec")) recursive = true;
                            if (t.equals("--sortAsc")) sortMode = 1;
                            if (t.equals("--sortDesc")) sortMode = -1;
                        }

                        am.addAction(actionListDirectory);
                        INode n = (INode) am.run();

                        List<Directory> directories = new ArrayList<>();
                        List<File> files = new ArrayList<>();

                        if (recursive) {
                            walk(((Directory) n), new Callback() {
                                @Override
                                public void exec(INode i) {
                                    if (i.getType().equals(INodeType.DIRECTORY)) {
                                        directories.add(((Directory) i));
                                    } else {
                                        files.add(((File) i));
                                    }
                                }
                            });
                        } else {
                            for (INode c : ((Directory) n).getChildren()) {
                                if (c.getType().equals(INodeType.DIRECTORY)) {
                                    directories.add(((Directory) c));
                                } else {
                                    files.add(((File) c));
                                }
                            }
                        }

                        if (directories.size() == 0 && files.size() == 0) {
                            System.out.println("Empty directory.");
                            break;
                        }

                        int finalSortMode = sortMode;
                        directories.sort(new Comparator<Directory>() {
                            @Override
                            public int compare(Directory o1, Directory o2) {
                                return o1.getName().compareTo(o2.getName()) * finalSortMode;
                            }
                        });

                        files.sort(new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName()) * finalSortMode;
                            }
                        });
                        if (showDirs)
                            for (Directory d : directories) {
                                System.out.println(d.getName() + "/");
                            }
                        if (showFiles)
                            for (File f : files) {
                                System.out.println(f.getName());
                            }
                        break;
                    }

                    case "mkdir":
                    case "touch":
                        if (args.length < 2) {
                            System.out.println("These arguments are required: \"name\".");
                            break;
                        }

                        String type = args[0].equals("mkdir") ? "DIR" : "FILE";
                        IAction addNode = new ActionINodeAdd(args[1], type);
                        am.addAction(addNode);
                        am.run();
                        System.out.println("Node added.");
                        break;

                    case "rm": {
                        if (args.length < 2) {
                            System.out.println("These arguments are required: \"path\".");
                            break;
                        }
                        IAction deleteNode = new ActionINodeDelete(args[1]);
                        am.addAction(deleteNode);
                        am.run();
                        System.out.println("Node deleted.");
                        break;
                    }

                    case "mv": {
                        if (args.length < 3) {
                            System.out.println("These arguments are required: \"target\", \"dest\".");
                            break;
                        }
                        IAction moveNode = new ActionINodeMove(args[1], args[2]);
                        am.addAction(moveNode);
                        am.run();
                        System.out.println("Node moved.");
                        break;
                    }

                    case "dl": {
                        if (args.length < 3) {
                            System.out.println("These arguments are required: \"target\", \"dest\".");
                            break;
                        }
                        IAction downloadNode = new ActionINodeDownload(args[1], args[2]);
                        am.addAction(downloadNode);
                        am.run();
                        System.out.println("Node downloaded.");
                        break;
                    }

                    case "upl": {
                        if (args.length < 3) {
                            System.out.println("These arguments are required: \"target\", \"source\".");
                            break;
                        }
                        IAction uploadNode = new ActionINodeUpload(args[1], args[2]);
                        am.addAction(uploadNode);
                        am.run();
                        System.out.println("Node uploaded.");
                        break;
                    }

                    /* --- LIMITS --- */

                    case "limit": {
                        if (args.length < 4) {
                            System.out.println("These arguments are required: \"path\", \"type\", \"limit\".");
                            break;
                        }
                        IAction addLimit;
                        if (args[2].equals(INodeLimitationType.MAX_SIZE.toString())) {
                            long max;
                            try {
                                max = Long.parseLong(args[3]);
                            } catch (Exception e) {
                                System.out.println(INodeLimitationType.MAX_SIZE.toString() +
                                        " limitation requires a valid \"limit\" long.");
                                break;
                            }
                            addLimit = new ActionAddLimit(args[1], args[2], max);
                        } else if (args[2].equals(INodeLimitationType.MAX_FILE_COUNT.toString())) {
                            long count;
                            try {
                                count = Long.parseLong(args[3]);
                            } catch (Exception e) {
                                System.out.println(INodeLimitationType.MAX_FILE_COUNT.toString() +
                                        " limitation requires a valid \"limit\" long.");
                                break;
                            }
                            addLimit = new ActionAddLimit(args[1], args[2], count);
                        } else if (args[2].equals(INodeLimitationType.BLACKLIST_EXT.toString())) {
                            addLimit = new ActionAddLimit(args[1], args[2], args[3]);
                        } else {
                            System.out.println("Invalid limitation type.");
                            break;
                        }
                        am.addAction(addLimit);
                        am.run();
                        System.out.println("Limitation added.");
                        break;
                    }

                    case "unlimit":
                        if (args.length < 4) {
                            System.out.println("These arguments are required: \"path\", \"type\", \"limit\".");
                            break;
                        }
                        IAction deleteLimit;
                        if (args[2].equals(INodeLimitationType.MAX_SIZE.toString())) {
                            long max;
                            try {
                                max = Long.parseLong(args[3]);
                            } catch (Exception e) {
                                System.out.println(INodeLimitationType.MAX_SIZE.toString() +
                                        " limitation requires a valid \"limit\" long.");
                                break;
                            }
                            deleteLimit = new ActionDeleteLimit(args[1], args[2], max);
                        } else if (args[2].equals(INodeLimitationType.MAX_FILE_COUNT.toString())) {
                            long count;
                            try {
                                count = Long.parseLong(args[3]);
                            } catch (Exception e) {
                                System.out.println(INodeLimitationType.MAX_FILE_COUNT.toString() +
                                        " limitation requires a valid \"limit\" long.");
                                break;
                            }
                            deleteLimit = new ActionDeleteLimit(args[1], args[2], count);
                        } else if (args[2].equals(INodeLimitationType.BLACKLIST_EXT.toString())) {
                            deleteLimit = new ActionDeleteLimit(args[1], args[2], args[3]);
                        } else {
                            System.out.println("Invalid limitation type.");
                            break;
                        }
                        am.addAction(deleteLimit);
                        am.run();
                        System.out.println("Limitation removed.");
                        break;

                    case "limits":
                        String path;
                        if (args.length < 2) {
                            path = ".";
                        } else {
                            path = args[1];
                        }
                        IAction getLimits = new ActionGetLimits(path);
                        am.addAction(getLimits);
                        Collection<INodeLimitation> limitations = null;
                        try {
                            limitations = (Collection<INodeLimitation>) am.run();
                        } catch (ClassCastException cce) {
                            System.out.println("PROGRAMMING ERROR: am.run() returned non-Collection<INodeLimitation> " +
                                    "type of object!");
                            System.exit(0);
                        }
                        if (limitations == null) break;

                        if (limitations.size() == 0) {
                            System.out.println("No limitations.");
                        } else {
                            for (INodeLimitation l : limitations) {
                                try {
                                    System.out.println(l.getType() + ": " + Arrays.toString((Object[]) l.getArgs()));
                                } catch (ClassCastException cce) {
                                    System.out.println("PROGRAMMING ERROR: am.run() returned non-Collection<INodeLimitation> " +
                                            "type of object!");
                                    System.exit(0);
                                }
                            }
                        }
                        break;

                    /* --- OTHER --- */

                    case "help":
                        System.out.println(
                                "Available commands:\n" +
                                        "--- STARTUP ---\n" +
                                        "init username password - Initialize the storage.\n" +
                                        "\n--- USER ---\n" +
                                        "login username password - Log in as an existing user.\n" +
                                        "logout - Log out.\n" +
                                        "users - Shows all existing users.\n" +
                                        "register username password - Add new user.\n" +
                                        "deregister username - Delete existing user.\n" +
                                        "\n--- PRIVILEGES ---\n" +
                                        "grant username privilege [object] - Grant privilege \"privilege\" to" +
                                        " user \"username\". Optionally, refer to [object].\n" +
                                        "revoke username privilege [object] - Revoke existing privilege from " +
                                        "user. Optionally, refer to [object].\n" +
                                        "\n--- STORAGE ---\n" +
                                        "cd path - Change current working directory to \"path\".\n" +
                                        "ls [path] - List contents of the directory. Optionally on \"path\".\n" +
                                        "dir [path] - Equivalent to ls.\n" +
                                        "mkdir name - Creates a new directory in the current folder.\n" +
                                        "touch name - Creates a new file in the current folder.\n" +
                                        "mv source dest - Moves the file or directory on source into directory on " +
                                        "dest.\n" +
                                        "dl target dest - Downloads file or directory on target into OS dest " +
                                        "directory.\n" +
                                        "upl dest target - Uploads file on OS target path onto local dest path.\n" +
                                        "rm path - Deletes the specified node.\n" +
                                        "\n--- OTHER ---\n" +
                                        "help - Print this menu.\n" +
                                        "exit - Quit the program."
                        );
                        break;

                    case "exit":
                        System.out.println("Bye!");
                        sc.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println(
                                "Invalid operation. Please try again."
                        );
                        break;
                }
            } catch (
                    IActionInsufficientPrivilegeException |
                            IActionUndoImpossibleException |
                            IComponentNotInitializedException |
                            IUserInitNotExistsException |
                            IUserDeleteNotExistException |
                            IActionBadParameterException |
                            IUserCannotDeleteCurrentUserException |
                            IUserDuplicateUsernameException |
                            DirectoryInvalidPathException |
                            INodeUnsupportedOperationException |
                            INodeFatalException
                            e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
            }

            //noinspection ConstantConditions
            if (
                    Core.getInstance().ConfigManager().getConfig() != null &&
                            Core.getInstance().StorageManager().getRoot() != null &&
                            Core.getInstance().UserManager().getUser() != null &&
                            Core.getInstance().UserManager().getUser().isAuthenticated() &&
                            Core.getInstance().UserManager().getUser().getCwd() != null) {
                //noinspection ConstantConditions
                System.out.print("~" + Core.getInstance().UserManager().getUser().getCwd().getPath() + " ");
            } else {
                System.out.print("> ");
            }
        }
    }

    /**
     * Prolazi kroz direktorijum rekurzivno.
     *
     * @param d Direktorijum kroz koji treba proći.
     */
    private static void walk(Directory d, Callback cb) {
        cb.exec(d);
        for(INode c : d.getChildren()) {
            if (c.getType().equals(INodeType.DIRECTORY)) {
                walk((Directory) c, cb);
            } else {
                cb.exec(c);
            }
        }
    }

    /**
     * Interfejs za prolazak kroz {@link #walk(Directory, Callback)} metodu.
     */
    private interface Callback {

        /**
         * Callback funkcija.
         *
         * @param i Trenutni INode.
         */
        void exec(INode i);
    }
}
