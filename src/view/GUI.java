package view;

import javax.swing.*;

/**
 * Created by Arcenal on 2/4/2016.
 */
public abstract class GUI {
    protected JFrame mCurrentWindow;

    protected abstract void initialize();

    public void showDialog(String title, String message) {
        if (mCurrentWindow == null) {
            System.err.print("Assign the root pane to show pop up dialog!");
        }
        if (title != null) {
            JOptionPane.showMessageDialog(mCurrentWindow, message, title, JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(mCurrentWindow, message);
        }
    }

}
