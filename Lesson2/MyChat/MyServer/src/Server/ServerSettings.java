package Server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ServerSettings {
    public static final int POS_X = 800;
    public static final int POS_Y = 550;
    public static final int WIDTH = 600;
    public static final int HEIGHT = 300;

    public static final int SERVER_PORT = 8189;

    public static final String LOG_PATH = "C:\\admin\\";
    public static final String CENSORSHIP_FILE = LOG_PATH + "censorship.txt";
    public static DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");
}
