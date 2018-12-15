package ch.quinella.launcher.launcher;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class LauncherFrame extends JFrame implements MouseMotionListener, MouseListener{

    private static LauncherFrame instance;
    private static LauncherPanel launcherPanel;
    private MouseEvent pressed;
    private Point location;

    public LauncherFrame(){

        this.setTitle("Underworld");
        this.setSize(850, 474);
        this.setUndecorated(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setContentPane(launcherPanel = new LauncherPanel());
        this.setResizable(true);


        this.setVisible(true);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

    }

    public void setFrameLocation(int x, int y){

        this.setLocation(x, y);

    }

    // Retourne l'instance de LauncherFrame
    public static LauncherFrame getInstance() {
        return instance;
    }


    // Retourne l'instance de notre progress bar
    public LauncherPanel getLauncherPanel(){
        return this.launcherPanel;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        e.getComponent().setLocation(e.getLocationOnScreen().x - pressed.getX(), e.getLocationOnScreen().y - pressed.getY());


    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

        pressed = e;

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
