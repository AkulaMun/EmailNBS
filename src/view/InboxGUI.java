package view;

import javax.swing.*;

/**
 * Created by Arcenal on 2/4/2016.
 */
public class InboxGUI {
    private JTabbedPane mRootTabbedPane;
    private JPanel mRootPanel;
    private JPanel mInboxPanel;
    private JPanel mOthersPanel;
    private JTextField textField1;
    private JComboBox comboBox1;
    private JTextArea textArea1;
    private JPanel mSettingPanel;
    private JButton mLogoutButton;
    private JTextArea textArea2;
    private JFrame mCurrentWindow;

    private InboxGUI(JFrame frame) {
        mCurrentWindow = frame;
        initialize();
    }

    public static InboxGUI newInstance(JFrame frame) {
        return new InboxGUI(frame);
    }

    private void initialize() {
        mCurrentWindow.setContentPane(mRootPanel);
        mCurrentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mCurrentWindow.pack();
        mCurrentWindow.setVisible(true);

        mLogoutButton.addActionListener(e -> LoginGUI.newInstance(mCurrentWindow));
        mRootTabbedPane.addChangeListener(e -> {
            System.out.println(mRootTabbedPane.getTitleAt(mRootTabbedPane.getSelectedIndex()));
        });

        //TODO: Add change listener to detect which tab was pressed
    }
}
