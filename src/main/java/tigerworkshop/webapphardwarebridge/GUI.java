package tigerworkshop.webapphardwarebridge;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tigerworkshop.webapphardwarebridge.interfaces.NotificationListenerInterface;
import tigerworkshop.webapphardwarebridge.services.ConfigService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class GUI implements NotificationListenerInterface {
    private static final Logger logger = LoggerFactory.getLogger("GUI");

    TrayIcon trayIcon;
    SystemTray tray;

    public static void main(String[] args) {
        GUI gui = new GUI();
        gui.launch();
    }

    public void launch() {
        Server server = new Server(this);

        try {
            JUnique.acquireLock(Constants.APP_ID);
        } catch (AlreadyLockedException e) {
            logger.error(Constants.APP_ID + " already running");
            System.exit(1);
        }

        ConfigService configService = ConfigService.getInstance();

        // Create tray icon
        try {
            if (!SystemTray.isSupported()) {
                System.out.println("SystemTray is not supported");
                return;
            }

            final Image image = ImageIO.read(GUI.class.getResource("/icon.png"));

            tray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(image, Constants.APP_NAME);

            // Create a pop-up menu components
            MenuItem settingItem = new MenuItem("Configurator");
            settingItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(configService.getConfig().getApiUri()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            MenuItem logItem = new MenuItem("Log");
            logItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().open(new File("log"));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });

            MenuItem restartItem = new MenuItem("Restart");
            restartItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    server.restart();
                }
            });

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    server.stop();
                    System.exit(0);
                }
            });

            //Add components to pop-up menu
            final PopupMenu popup = new PopupMenu();
            popup.add(settingItem);
            popup.add(logItem);
            popup.addSeparator();
            popup.add(restartItem);
            popup.add(exitItem);

            trayIcon.setPopupMenu(popup);

            tray.add(trayIcon);

            notify(Constants.APP_NAME, "is running in background!", TrayIcon.MessageType.INFO);
        } catch (Exception e) {
            System.out.println("TrayIcon could not be added.");
            e.printStackTrace();
        }

        server.start();
    }

    public void notify(String title, String message, TrayIcon.MessageType messageType) {
        try {
            trayIcon.displayMessage(title, message, messageType);
        } catch (Exception e) {
            
        }
    }
}
