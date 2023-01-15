package com.nuix.nx.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Toast {
    private int toastTimeInSeconds = 5;
    private String lastMessage = "";
    private Rectangle lastPosition = new Rectangle(0,0,0,0);

    public void setToastTimeInSeconds(int seconds) { toastTimeInSeconds = seconds; }
    public int getToastTimeInSeconds() { return toastTimeInSeconds; }

    public String getLastMessage() { return lastMessage; }

    public void showToast(String message, Rectangle position) {
        this.lastMessage = message;
        this.lastPosition = position;

        JWindow toastWindow = new JWindow();
        toastWindow.setBackground(new Color(0,0,0,0));
        toastWindow.setLayout(new FlowLayout());

        JTextArea toastPainter2 = new JTextArea(position.height/11, position.width/11);
        toastPainter2.setEditable(false);
        toastPainter2.setText(message);
        toastPainter2.setBackground(Color.LIGHT_GRAY);
        toastPainter2.setForeground(Color.BLACK);
        toastPainter2.setBorder(new EmptyBorder(10, 10, 10, 10));
        toastPainter2.setOpaque(true);
        toastPainter2.setLineWrap(true);
        toastPainter2.setWrapStyleWord(true);

        toastWindow.add(toastPainter2);
        toastWindow.setLocation(position.getLocation());
        toastWindow.setSize(position.getSize());

        animateIn(toastWindow);

        try {
            Thread.sleep(toastTimeInSeconds * 1000);
        } catch (InterruptedException e) {
            // Don't care;
        }

        animateOut(toastWindow);
    }

    public void showLastToast() {
        this.showToast(lastMessage, lastPosition);
    }

    private void animateIn(JWindow window) {
        try {
            window.setOpacity(0.0f);
            window.setVisible(true);
            while (window.getOpacity() < 1.0) {
                window.setOpacity(window.getOpacity() + 0.2f);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // Don't care
                }
            }
        } finally {
            window.setOpacity(1.0f);
        }
    }

    private void animateOut(JWindow window) {
        try {
            while(window.getOpacity() > 0.2) {
                window.setOpacity(window.getOpacity() - 0.2f);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Don't care
                }
            }
        } finally {
            window.setVisible(false);
            window.dispose();
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println(screenSize);
        Dimension toastSize = new Dimension(500, 80);
        Point location = new Point(screenSize.width - toastSize.width, screenSize.height - toastSize.height - 40);
        Rectangle bounds = new Rectangle(location, toastSize);
        System.out.println(bounds);
        new Toast().showToast("Nuix T3K Analysis\n" +
                "Your license for the application is about to expire.  Your last day of use is " +
                "2023-01-28.  Please contact your sales representative to extend it.",
                bounds);
        System.out.println("Done");
    }
}
