package ai.seitok.inoue;

import ai.seitok.inoue.dest.Destination;
import ai.seitok.inoue.dest.PostDestination;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.RescaleOp;
import java.net.MalformedURLException;
import java.net.URL;

public class ScreenCaptureUI implements MouseListener, MouseMotionListener {

    private static final RescaleOp DARKEN_OP = new RescaleOp(0.5f, 0, null);
    private static final Robot ROBOT;
    static {
        try {
            ROBOT = new Robot();
        } catch (AWTException e){
            throw new RuntimeException(e); // why are you running a screen capture tool on a non-graphics environment
        }
    }

    private final Inoue inoue;
    private final JFrame PARENT_FRAME;
    private final JLabel IMAGE_CONTAINER;
    private final Rectangle SCREEN_SIZE;
    private Destination dest;
    private BufferedImage capturedDesktop;
    private BufferedImage darkCapturedDesktop;
    private Point firstPt;
    private Point hoverPt;
    private Point secondPt;

    public ScreenCaptureUI(Inoue inoue){
        this.inoue = inoue;
        // debug
//        dest = new LocalDestination(Paths.get("D:/Repos/java/Inoue/test"));
        try {
            dest = new PostDestination("Mixtape.moe", "https://my.mixtape.moe", new URL("https://mixtape.moe/upload.php"), "files[]");
        } catch (MalformedURLException e){
            e.printStackTrace();
        }

        this.PARENT_FRAME = new JFrame("_ScreenCapture");
        this.SCREEN_SIZE = new Rectangle();
        Point uiAnchor = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for(GraphicsDevice dev : graphics.getScreenDevices()){
            for(GraphicsConfiguration config : dev.getConfigurations()){
                if(config.getBounds().x < uiAnchor.x)
                    uiAnchor.x = config.getBounds().x;
                if(config.getBounds().y < uiAnchor.y)
                    uiAnchor.y = config.getBounds().y;
                Rectangle2D.union(SCREEN_SIZE, config.getBounds(), SCREEN_SIZE);
            }
        }
        this.PARENT_FRAME.setUndecorated(true);
        this.PARENT_FRAME.setSize((int)SCREEN_SIZE.getWidth(), (int)SCREEN_SIZE.getHeight());
        this.PARENT_FRAME.setLocation(uiAnchor);
        this.PARENT_FRAME.add(IMAGE_CONTAINER = new JLabel(){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                paintCaptureRegion((Graphics2D)g);
            }
        });
        this.PARENT_FRAME.addMouseListener(this);
        this.PARENT_FRAME.addMouseMotionListener(this);
    }

    public void startCapture(){
        this.capturedDesktop = ROBOT.createScreenCapture(SCREEN_SIZE);
        this.darkCapturedDesktop = DARKEN_OP.filter(capturedDesktop, null);
        this.IMAGE_CONTAINER.setIcon(new ImageIcon(darkCapturedDesktop));
        this.PARENT_FRAME.setVisible(true);
    }

    public void paintCaptureRegion(Graphics2D g){
        Graphics2D overlay = (Graphics2D)g.create();
        if(firstPt != null && hoverPt != null){
            int sx = firstPt.x, sy = firstPt.y;
            int lx = hoverPt.x, ly = hoverPt.y;
            if(hoverPt.x < firstPt.x){
                sx = hoverPt.x;
                lx = firstPt.x;
            }

            if(hoverPt.y < firstPt.y){
                sy = hoverPt.y;
                ly = firstPt.y;
            }

            if(ly - sy <= 0 || lx - sx <= 0) return;
            overlay.drawImage(capturedDesktop.getSubimage(sx, sy, lx - sx, ly - sy), sx, sy, null);
        }
        overlay.dispose();
    }

    public void finishCapture(){
        // fix coords because windows has 0,0 as top left and 0,0 is java's bottom left
        if(secondPt.x < firstPt.x){
            int tmp = secondPt.x;
            secondPt.x = firstPt.x;
            firstPt.x = tmp;
        }

        if(secondPt.y < firstPt.y){
            int tmp = secondPt.y;
            secondPt.y = firstPt.y;
            firstPt.y = tmp;
        }

        RenderedImage capture = capturedDesktop.getSubimage(firstPt.x, firstPt.y, secondPt.x - firstPt.x, secondPt.y - firstPt.y);
        this.PARENT_FRAME.setVisible(false);

        darkCapturedDesktop.flush();
        capturedDesktop.flush();
        darkCapturedDesktop = null;
        capturedDesktop = null;
        IMAGE_CONTAINER.setIcon(null);
        firstPt = null;
        secondPt = null;

        inoue.getTrayIcon().displayMessage("Uploading...", "Inoue is uploading your screenshot.", TrayIcon.MessageType.INFO);
        StringSelection newClipboardContents = new StringSelection(dest.export(capture).toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(newClipboardContents, newClipboardContents);
        inoue.getTrayIcon().displayMessage("Finished uploading!", "The URL is in your clipboard!", TrayIcon.MessageType.INFO);
    }

    @Override
    public void mousePressed(MouseEvent e){
        firstPt = hoverPt = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e){
        secondPt = e.getPoint();
        finishCapture();
    }

    @Override
    public void mouseDragged(MouseEvent e){
        hoverPt = e.getPoint();
        this.PARENT_FRAME.repaint();
    }

    @Override public void mouseEntered(MouseEvent e){}
    @Override public void mouseExited(MouseEvent e){}
    @Override public void mouseClicked(MouseEvent e){}
    @Override public void mouseMoved(MouseEvent e){}

}
