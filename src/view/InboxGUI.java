package view;

import controller.MailboxController;
import utils.Resource;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.swing.*;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

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
    private JButton mLogoutButton;
    private JLabel mUsernameLabel;
    private JComboBox mMailboxComboBox;
    private JFrame mCurrentWindow;

    private InboxGUI(JFrame frame, Store store, String user) {
        mCurrentWindow = frame;
        mSessionStore = store;
        mUserName = user;
        initialize();
    }

    public static InboxGUI newInstance(JFrame frame, Store sessionStore, String userName) {
        frame.setPreferredSize(new Dimension(800, 1000));
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

        mUsernameLabel.setText(Resource.getStringResourceWithParam("username", mUserName));
        mLogoutButton.addActionListener(e -> LoginGUI.newInstance(mCurrentWindow));

        mMailboxController = MailboxController.getInstance();
        mMailboxController.setExceptionListener(this);

        mMailboxComboBox.removeAllItems();

        Set<String> mailFolders = mMailboxController.getFolderDisplayTitles();
        for(String mailFolder : mailFolders) {
            mMailboxComboBox.addItem(mailFolder);
        }

        if (mSessionStore == null) return;

        displayMessageTitles(mMailboxComboBox.getItemAt(0).toString());

        mMailboxComboBox.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                displayMessageTitles(e.getItem().toString());
            }
        });

        mMessageTitleComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                displayMessageContent(e.getItem().toString());
            }
        });
    }

    private void displayMessageTitles(String folderDisplayName) {
        mMailboxController.updateMessageList(mSessionStore, mMailboxController.getFolderName(folderDisplayName));
        mMessageContentTextArea.setText("");
        mMessageTitleComboBox.removeAllItems();
        List<String> mMessageTitleList = mMailboxController.getMessageTitles();
        for (String messageTitle : mMessageTitleList) {
            mMessageTitleComboBox.addItem(messageTitle);
        }
    }

    private void displayMessageContent(String messageSubject) {
        mMessageContentTextArea.setText( mMailboxController.getMessageContent(messageSubject));
    }

    @Override
    public void onException(MailboxController.ExceptionType e) {
        switch (e) {
            case SERVER_NOT_FOUND:
                showDialog(Resource.getStringResource("errorNoProviderTitle"), Resource.getStringResource("errorNoProviderMessage"));
                break;
            case LOGIN_FAILURE:
                showDialog(Resource.getStringResource("errorLoginTitle"), Resource.getStringResource("errorLoginMessage"));
                break;
            case FOLDER_ACCESS_FAILURE:
                showDialog(Resource.getStringResource("errorFolderAccessTitle"), Resource.getStringResource("errorFolderAccessMessage"));
                break;
            case CORRUPT_MESSAGE_TITLE:
                showDialog(Resource.getStringResource("errorMessageSubjectTitle"), Resource.getStringResource("errorMessageSubjectMessage"));
                break;
            case UNREADABLE_MESSAGE_CONTENT:
                showDialog(Resource.getStringResource("errorMessageContentTitle"), Resource.getStringResource("errorMessageContentMessage"));
                break;
        }
    }
}
