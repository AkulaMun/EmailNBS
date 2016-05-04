package view;

import controller.MailboxController;
import utils.Resource;

import javax.mail.Message;
import javax.mail.Store;
import javax.swing.*;

import java.awt.Dimension;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by Arcenal on 2/4/2016.
 */
public class InboxGUI extends GUI implements MailboxController.MailboxExceptionHandlerInterface{
    private MailboxController mMailboxController;
    private Store mSessionStore;
    private String mUserName;

    private JTabbedPane mRootTabbedPane;
    private JPanel mRootPanel;
    private JPanel mMailboxPanel;
    private JComboBox mMessageTitleComboBox;
    private JTextArea mMessageContentTextArea;
    private JPanel mSettingPanel;
    private JTextArea mTagTextArea;
    private JButton mLogOutButton;
    private JLabel mUsernameLabel;
    private JFrame mCurrentWindow;

    private InboxGUI(JFrame frame, Store store, String user) {
        mCurrentWindow = frame;
        mSessionStore = store;
        mUserName = user;
        initialize();
    }

    public static InboxGUI newInstance(JFrame frame, Store sessionStore, String userName) {
        return new InboxGUI(frame, sessionStore, userName);
    }

    //USED FOR DEBUG, BE SURE TO DELETE IT
    public static InboxGUI newInstance(JFrame frame) {
        frame.setPreferredSize(new Dimension(800, 1000));
        return new InboxGUI(frame, null, "Tovarich the End of The World");
    }

    protected void initialize() {
        mCurrentWindow.setContentPane(mRootPanel);
        mCurrentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mCurrentWindow.pack();
        mCurrentWindow.setVisible(true);

        mMailboxController = MailboxController.newInstance(this);

        mUsernameLabel.setText(Resource.getStringResourceWithParam("username", mUserName));
        mLogOutButton.addActionListener(e -> LoginGUI.newInstance(mCurrentWindow));
        mRootTabbedPane.addChangeListener(e -> {
            String tabTitle = mRootTabbedPane.getTitleAt(mRootTabbedPane.getSelectedIndex());

            List<Message> messageList = new ArrayList<>();

            switch (tabTitle) {
                case "Inbox":
                    messageList = mMailboxController.getMessages(mSessionStore, MailboxController.MailFolder.ALL);
                    break;
                case "Spam":
                    messageList = mMailboxController.getMessages(mSessionStore, MailboxController.MailFolder.SPAM);
                    break;
                case "Draft":
                    messageList = mMailboxController.getMessages(mSessionStore, MailboxController.MailFolder.DRAFT);
                    break;
                case "Sent":
                    messageList = mMailboxController.getMessages(mSessionStore, MailboxController.MailFolder.SENT);
                    break;
                case "Trash":
                    messageList = mMailboxController.getMessages(mSessionStore, MailboxController.MailFolder.TRASH);
                    break;
            }


        });
    }

    @Override
    public void onException(MailboxController.ExceptionType e) {
        if (e.equals(MailboxController.ExceptionType.SERVER_NOT_FOUND)) {
            showDialog(Resource.getStringResource("noProviderTitle"), Resource.getStringResource("noProviderMessage"));
        } else if (e.equals(MailboxController.ExceptionType.LOGIN_FAILURE)) {
            showDialog(Resource.getStringResource("loginFailureTitle"), Resource.getStringResource("loginFailureMessage"));
        }
    }
}
