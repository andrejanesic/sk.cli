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
            System.out.println("Greška u biranju drajvera, driver ne sme biti null!");
            System.exit(0);
        }

        try {
            if (driver == DriverTypes.LOCAL) {
                Class.forName("com.raf.sk.localImplementation.LocalImplementation");
            } else {
                Class.forName("com.raf.sk.RemoteImplementation.RemoteImplementation");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
