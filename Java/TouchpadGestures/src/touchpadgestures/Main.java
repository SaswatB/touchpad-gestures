/*
 * Copyright 2013 Saswat Bhattacharya
 * This file is part of Touchpad Gestures.
 * 
 * Touchpad Gestures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Touchpad Gestures is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Touchpad Gestures.  If not, see <http://www.gnu.org/licenses/>.
 */
package touchpadgestures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Saswat
 */
public class Main {

    private static Config c;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (args.length > 0 && args[0].equals("-s")) {
            try {
                Runtime.getRuntime().exec("java -jar \"" + Config.jar.getAbsolutePath() + "\" ");
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }

        try {
            if (lockApp("tglock52113")) {
                c = new Config();
                if (!(args.length > 0 && args[0].equals("-h"))) {
                    c.exec();//setVisible(true);
                }
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean lockApp(String lFile) throws java.io.IOException, InterruptedException {//TODO fix
        final java.io.File lockFile = new java.io.File(System.getProperty("java.io.tmpdir"), lFile);

        if (lockFile.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(lockFile))) {
                bw.write("hello?\n");
                bw.flush();
            }
            Thread.sleep(100);
            try (BufferedReader br = new BufferedReader(new FileReader(lockFile))) {
                String s = "";
                String t;
                while ((t = br.readLine()) != null) {
                    s = t;
                }
                switch (s.trim().toLowerCase()) {
                    case "hi":
                        //other instance responded
                        return false;
                    case "hello?":
                        //other instance did not respond
                        lockFile.delete();
                        break;
                    case "":
                        //can't read file
                        break;
                    default:
                    //junk
                }
            }
        }

        lockFile.createNewFile();
        lockFile.deleteOnExit();

        //Files.setAttribute(lockFile.toPath(), "dos:hidden", true);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(lockFile))) {
            bw.write("init\n");
            bw.flush();
        }

        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                long len = lockFile.length();
                while (true) {
                    if (len != lockFile.length()) {
                        len = lockFile.length();
                        try (BufferedReader br = new BufferedReader(new FileReader(lockFile))) {
                            String s = "";
                            String t;
                            while ((t = br.readLine()) != null) {
                                s = t;
                            }
                            switch (s.trim().toLowerCase()) {
                                case "hello?":
                                    //other instance responding
                                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(lockFile))) {
                                        bw.write("hi\n");
                                        bw.flush();
                                    }
                                    c.setVisible(true);
                                    break;
                                case "":
                                    //can't read file
                                    break;
                                default:
                                //junk
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    try {
                        Thread.sleep(90);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        thr.setDaemon(true);
        thr.start();

        return true;
    }
}
