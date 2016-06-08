import utils.Resource;
import view.InboxGUI;
import view.LoginGUI;

import javax.mail.Store;
import javax.swing.*;
import java.awt.*;

/**
 * Created by Arcenal on 2/4/2016.
 */
public class Main {
    private static JFrame mAppWindow;

    public static void main(String[] args) {
        mAppWindow = new JFrame(Resource.getStringResource("appTitle"));
        mAppWindow.setResizable(false);
        LoginGUI.newInstance(mAppWindow);
    }
}
