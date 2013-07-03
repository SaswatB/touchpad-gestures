
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

import com.java.tools.JXTrayIcon;
import java.awt.AWTException;
import java.awt.Font;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 *
 * @author Saswat
 */
public class Config extends javax.swing.JFrame {

    
    //TODO make exe local
    //public static File exe = new File("C:\\Users\\Saswat\\Dropbox\\Documents\\Visual Studio 2012\\Projects\\TouchpadGestures\\Release\\TouchpadGestures.exe");
    public static File jar;
    public static File exe;
    public static File config_file;
    public static File log;
    public static File startup_file = new File(new File(System.getProperty("user.home")), "AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\TouchpadGestures.bat");
    //Prefrences
    Preferences prefs = Preferences.userNodeForPackage(getClass());
    public static final String FIRSTRUNPREF = "first_run";
    public static final String AUTOSTARTPREF = "autostart";
    public static final String SILENTSTARTPREF = "silentstart";
    public static final String STARTUPPREF = "startup";
    public static final String JARLOCPREF = "jar_loc";

    static {
        try {
            jar = new File(URLDecoder.decode(Config.class.getProtectionDomain().getCodeSource().getLocation().getPath(),"UTF-8"));
            exe = new File(jar.getParentFile(), "TouchpadGestures.exe");
            config_file = new File(jar.getParentFile(), "config");
            log = new File(jar.getParentFile(),"log.txt");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Creates new form Config
     */
    public Config() {
        PrintStream out;
        
        System.out.println("Running: " + System.currentTimeMillis());
        
        try {
            out = new PrintStream(log);
            System.setOut(out);
            System.setErr(out);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        initComponents();
        setLocationRelativeTo(null);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        //<editor-fold defaultstate="collapsed" desc="System Tray Code">
        SystemTray st = SystemTray.getSystemTray();
        JXTrayIcon ti = new JXTrayIcon(new ImageIcon(getClass().getResource("/images/icon32.png")).getImage());
        JPopupMenu pm = new JPopupMenu();

        JMenuItem config = new JMenuItem("Config");
        config.setFont(config.getFont().deriveFont(Font.BOLD));
        config.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
            }
        });

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (proc != null) {
                    proc.destroy();
                }
                System.exit(0);
            }
        });

        pm.add(config);
        pm.add(new JSeparator());
        pm.add(exit);

        ti.setToolTip("Touchpad Gestures");
        ti.setJPopupMenu(pm);
        ti.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
            }
        });


        try {
            st.add(ti);
        } catch (AWTException ex) {//TODO handle
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Config File Loader">
        if (!config_file.exists()) {
            try {
                config_file.createNewFile();
            } catch (IOException ex) {//TODO handle
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(config_file))) {
            String line;
            int line_count = 0;
            while ((line = br.readLine()) != null) {
                line_count++;
                if (line.startsWith("//")) {
                    continue;
                }
                if (line.startsWith("s:")) {
                    int s = Integer.parseInt(line.substring(2).trim());
                    if (s > tab3sensitivity.getMaximum() || s < tab3sensitivity.getMinimum()) {
                        System.out.println("CONFIG ERROR: invalid sensitivity. line " + line_count);
                        continue;
                    }
                    sensitivity = s;
                    System.out.println(s);
                    tab3sensitivity.setValue(s);
                    jLabel8.setText(s + "");
                }
                if (line.startsWith("f:")) {
                    line = line.substring(2).trim();

                    if (!line.contains(",")) {
                        System.out.println("CONFIG ERROR: invalid gesture format(0). line " + line_count);
                        continue;
                    }

                    String parts[] = line.split(",");
                    if (parts.length < 3) {
                        System.out.println("CONFIG ERROR: invalid gesture format(1). line " + line_count);
                        continue;
                    }

                    Integer keys[] = new Integer[parts.length - 2];
                    for (int i = 2; i < parts.length; i++) {
                        keys[i - 2] = Integer.parseInt(parts[i].trim());
                    }
                    int f = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    gdata.add(new GestureData(f, g, keys));
                    //((DefaultListModel) tab2glist.getModel()).addElement(f + " Finger " + tab2gestures.getModel().getElementAt(g));
                }
            }
            refreshList();

        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>


        boolean first_run = prefs.getBoolean(FIRSTRUNPREF, true);
        if (first_run) {
            prefs.putBoolean(FIRSTRUNPREF, false);
            prefs.put(JARLOCPREF, jar.getAbsolutePath());
            prefs.putBoolean(STARTUPPREF, false);
            prefs.putBoolean(AUTOSTARTPREF, false);
            prefs.putBoolean(SILENTSTARTPREF, false);
        } else {
            if (prefs.getBoolean(STARTUPPREF, false)) {
                tab3startup.setSelected(true);
                if (!config_file.exists()) {
                    tab3startupActionPerformed(null);
                    JOptionPane.showMessageDialog(null, "Startup sequence was corrupted. It is now repaired.");
                } else {
                    try (BufferedReader br = new BufferedReader(new FileReader(config_file))) {
                        String s;
                        while ((s = br.readLine()) != null) {
                            if (s.startsWith("java -jar") && !s.substring(9).trim().contains(startup_file.getAbsolutePath())) {
                                tab3startupActionPerformed(null);
                                JOptionPane.showMessageDialog(null, "Startup sequence was corrupted. It is now repaired.");
                                break;
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            prefs.put(JARLOCPREF, jar.getAbsolutePath());
            tab3autostart.setSelected(prefs.getBoolean(AUTOSTARTPREF, false));
            tab3silent.setSelected(prefs.getBoolean(SILENTSTARTPREF, false));
        }

        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        tab1Start = new javax.swing.JButton();
        tab1Stop = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tab2glist = new javax.swing.JList();
        tab2add = new javax.swing.JButton();
        tab2remove = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        tab2fingers = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        tab2gestures = new javax.swing.JComboBox();
        tab2SelectKey = new javax.swing.JButton();
        tab2keydisplay = new javax.swing.JTextField();
        tab2save = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        tab3silent = new javax.swing.JCheckBox();
        tab3autostart = new javax.swing.JCheckBox();
        tab3startup = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        tab3sensitivity = new javax.swing.JSlider();
        jLabel8 = new javax.swing.JLabel();
        tab3savesens = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Touchpad Gesture Configuration");

        tab1Start.setText("Start");
        tab1Start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tab1StartActionPerformed(evt);
            }
        });

        tab1Stop.setText("Stop");
        tab1Stop.setEnabled(false);
        tab1Stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tab1StopActionPerformed(evt);
            }
        });

        jLabel2.setText("Status:");

        jLabel3.setText("Not Running");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(tab1Start, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tab1Stop, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(203, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tab1Stop)
                    .addComponent(tab1Start))
                .addContainerGap(231, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Status", jPanel1);

        jLabel4.setText("Gestures:");

        tab2glist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tab2glist.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                tab2glistValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(tab2glist);

        tab2add.setText("+");
        tab2add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tab2addActionPerformed(evt);
            }
        });

        tab2remove.setText("-");
        tab2remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tab2removeActionPerformed(evt);
            }
        });

        jLabel5.setText("Fingers:");

        tab2fingers.setModel(new javax.swing.SpinnerNumberModel(3, 3, 5, 1));
        tab2fingers.setEnabled(false);
        tab2fingers.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tab2fingersStateChanged(evt);
            }
        });

        jLabel6.setText("Gesture:");

        jLabel7.setText("Action:");

        tab2gestures.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tap", "Swipe Up", "Swipe Down", "Swipe Left", "Swipe Right" }));
        tab2gestures.setEnabled(false);
        tab2gestures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tab2gesturesActionPerformed(evt);
            }
        });

        tab2SelectKey.setText("Select Key Combo");
        tab2SelectKey.setEnabled(false);
        tab2SelectKey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tab2SelectKeyActionPerformed(evt);
            }
        });

        tab2keydisplay.setEditable(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tab2keydisplay)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tab2fingers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tab2gestures, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tab2SelectKey, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 15, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(tab2fingers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(tab2gestures, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(tab2SelectKey))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tab2keydisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(46, Short.MAX_VALUE))
        );

        tab2save.setText("Save");
        tab2save.setEnabled(false);
        tab2save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tab2saveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(tab2add)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tab2remove)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tab2save)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tab2add)
                    .addComponent(tab2remove)
                    .addComponent(tab2save))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Gestures", jPanel2);

        tab3silent.setText("Silent Start");
        tab3silent.setToolTipText("Open program directly to the system tray");
        tab3silent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tab3silentActionPerformed(evt);
            }
        });

        tab3autostart.setText("Auto Start");
        tab3autostart.setToolTipText("Run the gesture handler on program start");
        tab3autostart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tab3autostartActionPerformed(evt);
            }
        });

        tab3startup.setText("Run program on startup");
        tab3startup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tab3startupActionPerformed(evt);
            }
        });

        jLabel1.setText("Gesture Sensitivity");

        tab3sensitivity.setMaximum(50);
        tab3sensitivity.setPaintLabels(true);
        tab3sensitivity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tab3sensitivityStateChanged(evt);
            }
        });

        jLabel8.setText("10");

        tab3savesens.setText("Save Sensitivity");
        tab3savesens.setEnabled(false);
        tab3savesens.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tab3savesensActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tab3sensitivity, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tab3startup)
                                    .addComponent(tab3autostart)
                                    .addComponent(tab3silent)
                                    .addComponent(jLabel1))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tab3savesens)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(tab3startup)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tab3silent)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tab3autostart)
                .addGap(48, 48, 48)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tab3sensitivity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tab3savesens)
                .addContainerGap(59, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Options", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tab1StartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tab1StartActionPerformed
        try {
            //ProcessBuilder pb = new ProcessBuilder("C:\\Users\\Saswat\\Dropbox\\Documents\\Visual Studio 2012\\Projects\\TouchpadGestures\\Release\\TouchpadGestures.exe \""+config_file.getAbsolutePath()+"\"");
            //pb.inheritIO();
            proc = Runtime.getRuntime().exec(new String[]{exe.getAbsolutePath()}, null, config_file.getParentFile());
            //proc = pb.start();

            jLabel3.setText("Started.");

            Thread thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                        String s;
                        while ((s = br.readLine()) != null) {
                            jLabel3.setText(s);
                        }

                    } catch (IOException ex) {
                        Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (jLabel3.getText().equals("Started.")) {
                        jLabel3.setText("Stopped.");
                    }

                    tab1Start.setEnabled(true);
                    tab1Stop.setEnabled(false);
                }
            });
            thr.setDaemon(true);
            thr.start();

            tab1Start.setEnabled(false);
            tab1Stop.setEnabled(true);

        } catch (Exception ex) {
            jLabel3.setText("Could not start: " + ex.getMessage());
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_tab1StartActionPerformed

    private void tab1StopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tab1StopActionPerformed
        if (proc == null) {
            jLabel3.setText("Not Running.");
            return;
        }
        if (isProcessRunning(proc)) {
            proc.destroy();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (isProcessRunning(proc)) {
                jLabel3.setText("Cannot stop process.");
            } else {
                jLabel3.setText("Stopped.");
            }
        } else {
            jLabel3.setText("Stopped.");
            tab1Start.setEnabled(true);
            tab1Stop.setEnabled(false);
        }
    }//GEN-LAST:event_tab1StopActionPerformed

    private void tab2SelectKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tab2SelectKeyActionPerformed
        Integer keys[] = KeySelector.getKeys(this);
        if (keys == null || keys.length == 0) {
            return;
        }
        //gdata.get(tab2glist.getSelectedIndex()).setKeys(keys);
        tempkeys = keys;
        refreshKeyDisplay(keys);
        updateSave();
    }//GEN-LAST:event_tab2SelectKeyActionPerformed

    private void tab2addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tab2addActionPerformed
        ((DefaultListModel) tab2glist.getModel()).addElement("New...");
        try {
            tab2glist.setSelectedIndex(tab2glist.getModel().getSize() - 1);
        } catch (IndexOutOfBoundsException ex) {//idk
        }
        //gdata.add(new GestureData(3, 0, new Integer[]{}));
        tab2glistValueChanged(null);
        tab2add.setEnabled(false);
        tab2save.setEnabled(true);
    }//GEN-LAST:event_tab2addActionPerformed

    private void tab2removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tab2removeActionPerformed
        if (!tab2glist.getModel().getElementAt(tab2glist.getSelectedIndex()).equals("New...")) {
            gdata.remove(tab2glist.getSelectedIndex());
        }
        ((DefaultListModel) tab2glist.getModel()).remove(tab2glist.getSelectedIndex());
        tab2glistValueChanged(null);
        tab2add.setEnabled(true);
        tab2save.setEnabled(false);
    }//GEN-LAST:event_tab2removeActionPerformed

    private void tab2saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tab2saveActionPerformed

        int select = tab2glist.getSelectedIndex();

        for (int i = 0; i < gdata.size(); i++) {
            if (i != select) {
                GestureData gd = gdata.get(i);
                if (gd.fingers == ((Integer) tab2fingers.getValue()).intValue() && gd.gesture == tab2gestures.getSelectedIndex()) {
                    JOptionPane.showMessageDialog(this, "Duplicate Gestures not allowed", "Invalid Gesture", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        if (tab2glist.getModel().getElementAt(select).equals("New...")) {
            if (tempkeys == null || tempkeys.length == 0) {
                JOptionPane.showMessageDialog(this, "Gesture must contain a hotkey.", "Invalid Gesture", JOptionPane.ERROR_MESSAGE);
                return;
            }
            gdata.add(new GestureData(((Integer) tab2fingers.getValue()).intValue(), tab2gestures.getSelectedIndex(), tempkeys));
        } else {
            GestureData gd = gdata.get(select);
            gd.setFingers(((Integer) tab2fingers.getValue()).intValue());
            gd.setGesture(tab2gestures.getSelectedIndex());
            gd.setKeys(tempkeys);
        }
        rewriteConfig();
        refreshList();
        tab2add.setEnabled(true);
        tab2save.setEnabled(false);
    }//GEN-LAST:event_tab2saveActionPerformed

    private void tab2glistValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_tab2glistValueChanged
        int select = tab2glist.getSelectedIndex();

        boolean enable = select != -1;

        tab2remove.setEnabled(enable);
        tab2SelectKey.setEnabled(enable);
        tab2fingers.setEnabled(enable);
        tab2gestures.setEnabled(enable);
        if (!enable) {
            tab2keydisplay.setText("");
            tempkeys = null;
            return;
        }
        GestureData gd;
        if (tab2glist.getModel().getElementAt(select).equals("New...")) {
            gd = new GestureData(3, 0, new Integer[]{});
        } else {
            gd = gdata.get(select);
        }
        refreshKeyDisplay(tempkeys = gd.getKeys());
        tab2fingers.setValue(gd.fingers);
        tab2gestures.setSelectedIndex(gd.gesture);
    }//GEN-LAST:event_tab2glistValueChanged

    private void tab2fingersStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tab2fingersStateChanged
        updateSave();
    }//GEN-LAST:event_tab2fingersStateChanged

    private void tab2gesturesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tab2gesturesActionPerformed
        updateSave();
    }//GEN-LAST:event_tab2gesturesActionPerformed

    private void tab3sensitivityStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tab3sensitivityStateChanged
        jLabel8.setText(tab3sensitivity.getValue()+"");
        if (sensitivity != tab3sensitivity.getValue()) {
            tab3savesens.setEnabled(true);
        } else {
            tab3savesens.setEnabled(false);
        }
    }//GEN-LAST:event_tab3sensitivityStateChanged

    private void tab3savesensActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tab3savesensActionPerformed
        sensitivity = tab3sensitivity.getValue();
        rewriteConfig();
        tab3savesens.setEnabled(false);
    }//GEN-LAST:event_tab3savesensActionPerformed

    private void tab3startupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tab3startupActionPerformed
        if (tab3startup.isSelected()) {
            if (!startup_file.exists()) {
                try {
                    System.out.println(startup_file);
                    startup_file.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            }
            try (PrintWriter writer = new PrintWriter(startup_file)) {
                writer.write("\n");
                writer.write("cd "+ jar.getParent() +"\n");
                writer.write("java -jar \"" + jar.getAbsolutePath() + "\" -s\n");
                writer.write("\n");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        } else {
            if (startup_file.exists()) {
                startup_file.delete();
            }
        }
        prefs.putBoolean(STARTUPPREF, tab3startup.isSelected());
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_tab3startupActionPerformed

    private void tab3silentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tab3silentActionPerformed
        prefs.putBoolean(SILENTSTARTPREF, tab3silent.isSelected());
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_tab3silentActionPerformed

    private void tab3autostartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tab3autostartActionPerformed
        prefs.putBoolean(AUTOSTARTPREF, tab3autostart.isSelected());
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_tab3autostartActionPerformed

    private void updateSave() {
        int select = tab2glist.getSelectedIndex();
        if (tab2glist.getModel().getElementAt(select).equals("New...")) {
            return;
        }
        GestureData gd = gdata.get(select);
        if (gd.fingers != ((Integer) tab2fingers.getValue()).intValue() || gd.gesture != tab2gestures.getSelectedIndex() || (tempkeys != null && gd.keys.length != tempkeys.length)) {
            tab2save.setEnabled(true);
            return;
        } else {
            if (tempkeys != null) {
                for (int i = 0; i < tempkeys.length; i++) {
                    if (tempkeys[i] != gd.keys[i]) {
                        tab2save.setEnabled(true);
                        return;
                    }
                }
            }
        }
        tab2save.setEnabled(false);
    }

    private void refreshKeyDisplay(Integer[] keys) {
        if (keys == null) {
            tab2keydisplay.setText(null);
        }
        String text = "";
        for (int key : keys) {
            if (key == 91) {//Windows key override
                text += "Win + ";
                continue;
            }
            if (KeySelector.char2vk.containsKey(key)) {
                text += KeySelector.intercaps(KeySelector.char2vk.get(key).substring(3)) + " + ";
            }
        }
        if (!text.isEmpty()) {
            text = text.substring(0, text.length() - 3);
        }
        tab2keydisplay.setText(text);
    }

    private void refreshList() {
        DefaultListModel<String> dlm = new DefaultListModel<>();
        for (GestureData gd : gdata) {
            dlm.addElement(gd.fingers + " Finger " + tab2gestures.getModel().getElementAt(gd.gesture));
        }
        tab2glist.setModel(dlm);
    }

    private void rewriteConfig() {
        try (PrintWriter writer = new PrintWriter(config_file)) {
            if (sensitivity != 10) {
                writer.write("s: " + sensitivity + "\n");
            }
            for (GestureData gd : gdata) {
                writer.write("f: " + gd.fingers + "," + gd.gesture + "," + Arrays.toString(gd.getKeys()).replace("[", "").replace("]", "") + "\n");
            }
            if(tab1Stop.isEnabled()) {
                tab1StopActionPerformed(null);
                tab1StartActionPerformed(null);
            }
        } catch (FileNotFoundException ex) {//TODO handle
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public static boolean isProcessRunning(Process proc) {
        try {
            proc.exitValue();
            return false;
        } catch (IllegalThreadStateException ex) {
            return true;
        }
    }

    public void exec() {
        if (!prefs.getBoolean("silentstart", false)) {
            setVisible(true);
        }
        if (prefs.getBoolean("autostart", false)) {
            tab1StartActionPerformed(null);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Config.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Config().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton tab1Start;
    private javax.swing.JButton tab1Stop;
    private javax.swing.JButton tab2SelectKey;
    private javax.swing.JButton tab2add;
    private javax.swing.JSpinner tab2fingers;
    private javax.swing.JComboBox tab2gestures;
    private javax.swing.JList tab2glist;
    private javax.swing.JTextField tab2keydisplay;
    private javax.swing.JButton tab2remove;
    private javax.swing.JButton tab2save;
    private javax.swing.JCheckBox tab3autostart;
    private javax.swing.JButton tab3savesens;
    private javax.swing.JSlider tab3sensitivity;
    private javax.swing.JCheckBox tab3silent;
    private javax.swing.JCheckBox tab3startup;
    // End of variables declaration//GEN-END:variables
    Process proc;
    ArrayList<GestureData> gdata = new ArrayList<>();
    Integer[] tempkeys;
    int sensitivity = 10;

    class GestureData {

        private int fingers;
        private int gesture;
        private Integer[] keys;

        public GestureData(int fingers, int gesture, Integer[] keys) {
            this.fingers = fingers;
            this.gesture = gesture;
            this.keys = keys;
        }

        public int getFingers() {
            return fingers;
        }

        public int getGesture() {
            return gesture;
        }

        public Integer[] getKeys() {
            return keys;
        }

        public void setFingers(int fingers) {
            this.fingers = fingers;
        }

        public void setGesture(int gesture) {
            this.gesture = gesture;
        }

        public void setKeys(Integer[] keys) {
            this.keys = keys;
        }
    }
}
