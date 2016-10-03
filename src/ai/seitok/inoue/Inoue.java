package ai.seitok.inoue;

import ai.seitok.inoue.hook.NativeHook;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Inoue {

    private final TrayIcon trayIcon;
    private final NativeHook hook;
    private final ScreenCaptureUI CAPTURE_UI = new ScreenCaptureUI(this);

    public static void main(String[] args) throws Throwable {
        new Inoue();
    }

    public Inoue() throws Throwable { // bad
        hook = new NativeHook(0x2c, CAPTURE_UI::startCapture); // Hook print screen key
        hook.hookKey();

        BufferedImage icon = ImageIO.read(Inoue.class.getClassLoader().getResourceAsStream("icon.png"));
        int fakeIconLen = new TrayIcon(icon).getSize().width;
        trayIcon = new TrayIcon(icon.getScaledInstance(fakeIconLen, -1, Image.SCALE_SMOOTH));

        SystemTray.getSystemTray().add(trayIcon);
        Runtime.getRuntime().addShutdownHook(new Thread(hook::unhookKey));

        trayIcon.displayMessage("Inoue is running!", "Hit Print Screen/\"Prt Scr\" to start taking a screenshot.", TrayIcon.MessageType.INFO);
    }

    public TrayIcon getTrayIcon(){
        return trayIcon;
    }

}
