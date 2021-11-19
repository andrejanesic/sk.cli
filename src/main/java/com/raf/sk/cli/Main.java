package com.raf.sk.cli;

import com.raf.sk.core.actions.*;
import com.raf.sk.core.core.Core;
import com.raf.sk.core.exceptions.*;
import com.raf.sk.core.user.IPrivilege;
import com.raf.sk.core.user.IUser;

import java.util.Collection;
import java.util.Scanner;

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
                    } else if (argv[i + 1].equals("google")) {
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
                Class.forName("com.raf.sk.RemoteImplementation.RemoteImplementation");
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
                        initStorage = new ActionInitStorage(cwd);
                        am.addAction(initStorage);
                        am.run();
                        System.out.println("[3/3] Tree loaded...");
                        System.out.println("Storage ready!");
                        break;
                    }

                    case "addLimit":
                        // #TODO
                        break;
                    case "cd":
                        // #TODO changeCwd
                        break;

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

                    case "deleteLimit":
                        // #TODO
                        break;
                    case "deleteUser":
                        // #TODO
                        break;
                    case "getLimits":
                        // #TODO
                        break;
                    case "nodeAdd":
                        // #TODO
                        break;
                    case "nodeDelete":
                        // #TODO
                        break;
                    case "nodeDownload":
                        // #TODO
                        break;
                    case "nodeMove":
                        // #TODO
                        break;
                    case "nodeUpload":
                        // #TODO
                        break;
                    case "help":
                        System.out.println(
                                "Available commands:\n" +
                                        "--- STARTUP ---\n" +
                                        "init username password - Initialize the storage\n" +
                                        "\n--- USER ---\n" +
                                        "login username password - Log in as an existing user\n" +
                                        "logout - Log out\n" +
                                        "users - Shows all existing users\n" +
                                        "register username password - Add new user\n" +
                                        "deregister username - Delete existing user\n" +
                                        "\n--- PRIVILEGES ---\n" +
                                        "grant username privilege [object] - Grant privilege \"privilege\" to" +
                                        " user \"username\". Optionally, refer to [object]\n" +
                                        "revoke username privilege [object] - Revoke existing privilege from " +
                                        "user. Optionally, refer to [object]\n" +
                                        "\n--- STORAGE ---\n" +
                                        "cd path - Change current working directory to \"path\" (relative or" +
                                        "absolute path.)\n" +
                                        "\n--- OTHER ---\n" +
                                        "help - Print this menu\n" +
                                        "exit - Quit the program"
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
                            IUserDuplicateUsernameException
                            e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            System.out.print("> ");
        }
    }
}
