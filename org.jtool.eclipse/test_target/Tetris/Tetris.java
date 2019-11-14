/*
 *     Tetris.java
 *
 *     (C) 2010 Katsuhisa Maruyama (maru@cs.ritsumei.ac.jp)
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Tetris extends JFrame implements KeyListener, Runnable {

    private Pit pit;
    private GameInfo info;

    private Thread timer = null;

    protected static final int COLUMNS = 10;
    protected static final int ROWS = 22;

    public static void main(String argv[]) {
        Tetris tetris = new Tetris();
        tetris.init();
    }

    public void init() {
        info = new GameInfo();
        pit = new Pit(info);

        Container cp = getContentPane();
        cp.setLayout(new FlowLayout(FlowLayout.CENTER));
        cp.add(pit);
        cp.add(info);
        pack();
        setVisible(true);

        pit.init();
        info.init();
        pit.placeNextBlock();
        info.update(pit.getNextBlock());

        pit.addKeyListener(this);
        info.addKeyListener(this);
        requestFocus();
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_S) {
            createTimer();
            pit.gameStart();
            info.gameStart();
            pit.placeNextBlock();
            pit.update();
            info.update(pit.getNextBlock());
            startTimer();

        } else if (keyCode == KeyEvent.VK_Q) {
            System.exit(0);
        }

        if (timer != null) {
            Block block = pit.getCurrentBlock();
            switch (keyCode) {
                case KeyEvent.VK_B:
                    block.moveLeft();
                    break;
                case KeyEvent.VK_M:
                    block.moveRight();
                    break;
                case KeyEvent.VK_N:
                    block.rotate();
                    break;
                case KeyEvent.VK_Z:
                    block.drop();
                    break;
            }
            pit.update();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    private void createTimer() {
        if (timer != null) {
            stopTimer();
        }
        timer = new Thread(this);
    }

    private void startTimer() {
        timer.start();
    }

    private void stopTimer() {
        timer = null;
    }

    public void run() {
        boolean isGameOver = false;
        Thread thisThread = Thread.currentThread();
        while (timer == thisThread) {
   
            try {
                Thread.sleep(info.getSpeed());
            } catch (InterruptedException e) { }

            Block block = pit.getCurrentBlock();
            if (!block.fall()) {
                block = null;  // delete block
                pit.checkLines();
                isGameOver = !pit.placeNextBlock();
                info.update(pit.getNextBlock());
            }
            pit.update();

            if (isGameOver) {
                pit.gameOver();
                stopTimer();
            }
        }
    }
}
