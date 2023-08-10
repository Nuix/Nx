package com.nuix.nx.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A Toast is a short-lived informational message displayed in the UI.
 * <p>
 *     Generally, Toasts are displayed in the bottom right corner of the UI - either the screen or the window.  They
 *     are meant for non-critical information display not required by the current user workflow.  For example if a
 *     licenses was about to expire, a Toast might be used to display the time left until expiration.
 * </p>
 * <p>
 *     This Toast class represents a reusable container that can display text in a user-defined position on the screen.
 *     Although the only limits to the position are that they must be fully on-screen, it will be generally useful to
 *     ensure it is positions towards the bottom of the display as the Toast will animate on screen from below.  This
 *     Toast will have an in-animation that moves the message into view from off the bottom of the screen and an out-
 *     animation that fades the screen out.  Animations are handled in a thread to avoid blocking the UI or scripting
 *     thread context
 * </p>
 * <p>
 *     Use the {@link #showToast(String, Rectangle)} method to display the message in the location of interest:
 * </p>
 * <pre>
 *         Toast toast = new Toast();
 *
 *         String message = "Title\nThis is a brief message about something, but no worries, won't kill the application.";
 *         Rectangle position = new Rectangle(1400, 1000, 500, 80);
 *         toast.showToast(message, position);
 * </pre>
 * <p>
 *     TODO: I have found the threading can cause slower than expected motion when the computer is under heavy load.
 * </p>
 */
public class Toast {
    private int toastTimeInSeconds = 5;
    private String lastMessage = "";
    private Rectangle lastPosition = new Rectangle(0,0,0,0);

    /**
     * @param seconds The period of time, in seconds, the Toast should remain on screen after the in-animation
     *                completes and before the out-animation starts.  The full time on screen will be longer than this
     *                as the animation runs.  Defaults to 5 seconds if not set.
     */
    public void setToastTimeInSeconds(int seconds) { toastTimeInSeconds = seconds; }

    /**
     * @return The period of time, in seconds, the Toast will be displayed on screen after it reaches its destination
     * and before it begins to fade out.  The actual time on screen will be longer than this as the animations run.
     */
    public int getToastTimeInSeconds() { return toastTimeInSeconds; }

    /**
     * @return The string contents of the last message sent to the Toast
     */
    public String getLastMessage() { return lastMessage; }

    /**
     * Display the provided message at the position specified.
     * <p>
     *     When this method is called, the Toast will be created and animate in by sliding up to the desired position
     *     from off the bottom of the screen.  Once in position it holds for several seconds then fades away.
     *     This tool does not manage the display of multiple toasts at one time.
     * </p>
     * @param message A message to display.  The message can be multi-line, can include line breaks (\n) and will word
     *                wrap.
     * @param targetPosition A {@link Rectangle} defining the location and size of the box to display the message in.
     *                       The location is relative to the top left of the screen (not window).  The width will be
     *                       fixed at the defined size, and the height will be maximized at the defined size (but
     *                       could be smaller if the message wouldn't fill the area).
     * @throws RuntimeException if the target position is illegal, such as fully or partially off-screen.
     */
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
        JTextArea toastPainter = new JTextArea((targetPosition.height-borderSize*2)/fontHeight,
                (targetPosition.width-borderSize*2)/charWidth);

        // Other text area configurations
        toastPainter.setText(message);
        toastPainter.setFont(displayFont);

        toastPainter.setEditable(false);
        toastPainter.setBackground(new Color(230, 230, 230));
        toastPainter.setForeground(Color.BLACK);
        toastPainter.setBorder(new EmptyBorder(borderSize, borderSize, borderSize, borderSize));
        toastPainter.setOpaque(true);
        toastPainter.setLineWrap(true);
        toastPainter.setWrapStyleWord(true);

        toastWindow.add(toastPainter);

        // The expected size of the text display
        Dimension painterSize = toastPainter.getPreferredSize();
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

    /**
     * Reshow the last message that was displayed, in the same location it was displayed in.
     */
    public void showLastToast() {
        this.showToast(lastMessage, lastPosition);
    }
}
