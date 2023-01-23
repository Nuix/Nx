package com.nuix.nx.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toast {
    private int toastTimeInSeconds = 5;
    private String lastMessage = "";
    private Rectangle lastPosition = new Rectangle(0,0,0,0);

    public void setToastTimeInSeconds(int seconds) { toastTimeInSeconds = seconds; }
    public int getToastTimeInSeconds() { return toastTimeInSeconds; }

    public String getLastMessage() { return lastMessage; }

    public void showToast(String message, Rectangle targetPosition) {
        if (targetPosition.x < 0 || targetPosition.y < 0)
            throw new RuntimeException("Target position location must be >= 0");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (targetPosition.x > (screenSize.width - targetPosition.width) ||
            targetPosition.y > (screenSize.height - targetPosition.height))
            throw new RuntimeException("Target position must not be off screen.");

        this.lastMessage = message;
        this.lastPosition = targetPosition;

        JWindow toastWindow = new JWindow();
        toastWindow.setBackground(new Color(0,0,0,0));
        toastWindow.setLayout(new FlowLayout());

        // Get the size of the characters in the font, using "M" as a representative character for width
        Font displayFont = new Font(Font.SERIF, Font.PLAIN, 12);
        FontMetrics metrics = toastWindow.getFontMetrics(displayFont);
        int fontHeight = metrics.getHeight();
        int charWidth = metrics.charWidth('M');

        int borderSize = 10;
        // Create a Text Area - define the number of Rows and Columns to display based on the sized of the character
        JTextArea toastPainter2 = new JTextArea((targetPosition.height-borderSize*2)/fontHeight,
                (targetPosition.width-borderSize*2)/charWidth);

        // Other text area configurations
        toastPainter2.setText(message);
        toastPainter2.setFont(displayFont);

        toastPainter2.setEditable(false);
        toastPainter2.setBackground(new Color(230, 230, 230));
        toastPainter2.setForeground(Color.BLACK);
        toastPainter2.setBorder(new EmptyBorder(borderSize, borderSize, borderSize, borderSize));
        toastPainter2.setOpaque(true);
        toastPainter2.setLineWrap(true);
        toastPainter2.setWrapStyleWord(true);

        toastWindow.add(toastPainter2);

        // The expected size of the text display
        Dimension painterSize = toastPainter2.getPreferredSize();
        // Set the x position = the desired position + desired width - expected with
        int displayX = (targetPosition.x + targetPosition.width) - painterSize.width;
        Point startingPosition = new Point(displayX, screenSize.height);

        toastWindow.setLocation(startingPosition);
        toastWindow.setSize(painterSize);


        toastWindow.setOpacity(1.0f);
        toastWindow.setVisible(true);

        Timer animateOutTask = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toastWindow.setOpacity(toastWindow.getOpacity() - 0.04f);

                if (toastWindow.getOpacity() < 0.04f) {
                    toastWindow.setVisible(false);
                    toastWindow.dispose();

                    ((Timer)e.getSource()).stop();
                }
            }
        });
        animateOutTask.setRepeats(true);
        animateOutTask.setInitialDelay(toastTimeInSeconds * 1000);


        Timer animateInTask = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point currentPosition = toastWindow.getLocation();
                Point newPosition = new Point(currentPosition.x, currentPosition.y - 5);
                toastWindow.setLocation(newPosition);

                if (newPosition.y <= targetPosition.y) {
                    ((Timer)e.getSource()).stop();
                    animateOutTask.start();
                }
            }
        });
        animateInTask.setRepeats(true);
        animateInTask.setInitialDelay(0);
        animateInTask.start();
    }

    public void showLastToast() {
        this.showToast(lastMessage, lastPosition);
    }

    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension toastSize = new Dimension(500, 80);
        Point location = new Point(screenSize.width - toastSize.width, screenSize.height - toastSize.height - 40);
        Rectangle bounds = new Rectangle(location, toastSize);

        new Toast().showToast("Nuix T3K Analysis\n" +
                "Your license for the application is about to expire.  Your last day of use is " +
                "2023-01-28.  Please contact your sales representative to extend it.",
                bounds);
    }
}
