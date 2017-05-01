package tothemoon.GUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;
import tothemoon.MyAction;

//@author Michael Haertling
public class GalaxyView extends JPanel implements KeyListener, MouseListener, MouseWheelListener, MouseMotionListener {

    BufferedImage buffer;
    boolean upPressed = false, leftPressed = false, rightPressed = false;

    double viewX = 0, viewY = 0, zoom = 1;
    double zoomShiftX = 0, zoomShiftY = 0;
    final double zoomIncrement = 0.1;
    double radius;

    //Statisctics
    int generation;
    int rocketsLeft;
    long timeInGeneration;

    double[] moonVector;
    double[] earthVector;
    boolean canSeeEarth;
    boolean canSeeMoon;
    boolean needsUpdate = true;
    

    Point mousePress;
    
    MyAction swapView;

    public GalaxyView(MyAction swap) {
        this.setBackground(Color.BLACK);
        swapView = swap;
    }

    @Override
    public void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        Graphics2D g = (Graphics2D) gr;
        g.drawImage(buffer, 0, 0, this);

        //Generate the stats
        ArrayList<String> stats = new ArrayList<>();
        stats.add("Generation: " + generation);
        stats.add("Rockets Remaining: " + rocketsLeft);
        stats.add("Generation Duration: " + timeInGeneration);

        //Draw the stats
        g.setColor(Color.yellow);
        int statX = 10;
        int statY = 20;
        final int statInc = 15;
        for (String stat : stats) {
            g.drawString(stat, statX, statY);
            statY += statInc;
        }

        radius = (int) (Math.min(getWidth() / 2, getHeight() / 2));
        //Draw the earth locator
        if (!this.canSeeEarth && earthVector != null) {
            g.drawString("EARTH", (int) (radius * earthVector[0]+getWidth()/2), (int) (radius * earthVector[1]+getHeight()/2));
        }

        //Draw the moon locator
        if (!this.canSeeMoon && moonVector != null) {
            g.drawString("MOON", (int) (radius * moonVector[0]+getWidth()/2), (int) (radius * moonVector[1]+getHeight()/2));
        }

    }

    public Graphics2D getBufferGraphics() {
        int size = (int) (this.getWidth());
        buffer = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        return (Graphics2D) buffer.getGraphics();
    }

    public boolean needsUpdate() {
        return needsUpdate;
    }

    public double getZoom() {
        return zoom;
    }

    public void updateStats(int generation, int rocketsLeft, long time) {
        this.generation = generation;
        this.rocketsLeft = rocketsLeft;
        this.timeInGeneration = time;
    }

    public void updateVectors(boolean seeEarth, boolean seeMoon, double[] ev, double[] mv) {
        this.canSeeEarth = seeEarth;
        this.canSeeMoon = seeMoon;
        this.earthVector = ev;
        this.moonVector = mv;
        this.needsUpdate = false;
    }

    public double getRadius(){
        return radius;
    }
    
    public boolean isUpPressed() {
        return upPressed;
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public double getViewX() {
        return viewX;
    }

    public double getViewY() {
        return viewY;
    }

    public void setViewX(double x) {
        viewX = x;
        this.needsUpdate = true;
    }

    public void setViewY(double y) {
        viewY = y;
        this.needsUpdate = true;
    }

    public void shiftViewX(double x) {
        viewX += x;
        this.needsUpdate = true;
    }

    public void shiftViewY(double y) {
        viewY += y;
        this.needsUpdate = true;
    }

    public void setViewPosition(double x, double y) {
        setViewX(x);
        setViewY(y);
    }

    public void setZoom(double z) {
        if (z < 0) {
            return;
        }

        double halfWidth = getWidth() / 2;
        double halfHeight = getHeight() / 2;

        double tvx = viewX + halfWidth / zoom;
        double tvy = viewY + halfHeight / zoom;

        tvx -= halfWidth / z;
        tvy -= halfHeight / z;

        zoom = z;

        viewX = tvx;
        viewY = tvy;
        this.needsUpdate = true;

    }


    public void swap(){
        swapView.act();
    }

    @Override
    public void keyTyped(KeyEvent ke) {

    }

    @Override
    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_UP) {
            upPressed = true;
        } else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
            leftPressed = true;
        } else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        } else if (ke.getKeyCode() == KeyEvent.VK_F) {
            swap();

        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_UP) {
            upPressed = false;
        } else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
            leftPressed = false;
        } else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        swap();
    }

    @Override
    public void mousePressed(MouseEvent me) {
        mousePress = me.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
        double newZ = zoom/Math.pow(1.4, mwe.getWheelRotation());

        this.setZoom(newZ);
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        this.setViewPosition(viewX - (me.getX() - this.mousePress.getX()) / zoom, viewY - (me.getY() - this.mousePress.getY()) / zoom);
        mousePress = me.getPoint();
    }

    @Override
    public void mouseMoved(MouseEvent me) {
    }

}
