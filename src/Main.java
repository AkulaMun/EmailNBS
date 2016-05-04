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
    private static Store mAccountStore;

    public static void main(String[] args) {
        mAppWindow = new JFrame(Resource.getStringResource("appTitle"));
        mAppWindow.setResizable(false);
        mAppWindow.setPreferredSize(new Dimension(800, 600));
        //LoginGUI.newInstance(mAppWindow);
        InboxGUI.newInstance(mAppWindow);
    }
}
