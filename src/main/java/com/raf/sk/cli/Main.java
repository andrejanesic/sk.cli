package com.raf.sk.cli;

import com.raf.sk.core.actions.*;
import com.raf.sk.core.core.Core;
import com.raf.sk.core.exceptions.IActionInsufficientPrivilegeException;

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

        System.out.println("Enter your command: ");
        while (true) {
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
                        case "addLimit":
                            // #TODO
                            break;
                        case "addUser":
                            // #TODO
                            break;
                        case "cd":
                            // #TODO changeCwd
                            break;
                        case "logout":
                            // #TODO deinitUser
                            break;
                        case "deleteLimit":
                            // #TODO
                            break;
                        case "deleteUser":
                            // #TODO
                            break;
                        case "getLimits":
                            // #TODO
                            break;
                        case "grant":
                            // #TODO grantPrivilege
                            break;
                        case "init":
                            IAction initConfig;
                            if (args.length < 3) {
                                System.out.println("These arguments are required: \"username\", \"password\"");
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
                        case "revoke":
                            // #TODO revokePrivilege
                            break;
                        case "help":
                            System.out.println(
                                    "Available commands:\n" +
                                            "\tinit username password - Initialize the storage;\n" +
                                            "\tcd dir - Change current working directory.\n" +
                                            "\t--- USER ---\n" +
                                            "\taddUser username password - Add new user\n" +
                                            "\tdeleteUser username - Delete existing user\n" +
                                            "\tlogout - Log out\n" +
                                            "\t--- PRIVILEGES ---\n" +
                                            "\tgrant username privilege [object] - Grant privilege \"privilege\" to" +
                                            " user \"username\". Optionally, refer to [object]" +
                                            "\trevoke username privilege [object] - Revoke existing privilege from " +
                                            "user. Optionally, refer to [object]" +
                                            "\t--- STORAGE ---\n" +
                                            "\t"
                            );
                        case "exit":
                            System.out.println(
                                    "Doviđenja!"
                            );
                            System.exit(0);
                            break;
                        default:
                            System.out.println(
                                    "Invalid operation. Please try again."
                            );
                            break;
                    }
                } catch (IActionInsufficientPrivilegeException e) {
                    System.out.println(e.getMessage());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}
