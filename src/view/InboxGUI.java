package view;

import controller.MailboxController;
import utils.Resource;

import javax.mail.Store;
import javax.swing.*;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.List;

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
    private JPanel mTagSettingPanel;
    private JComboBox mMessageTitleComboBox;
    private JTextArea mMessageContentTextArea;
    private JTextArea mTagTextArea;
    private JButton mLogoutButton;
    private JLabel mUsernameLabel;
    private JComboBox mMailboxComboBox;
    private JLabel mCacheStatusLabel;
    private JButton mTagAllMailsWithFolderName;
    private JButton mRefreshCacheButton;
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

    protected void initialize() {
        mCurrentWindow.setContentPane(mRootPanel);
        mCurrentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mCurrentWindow.pack();
        mCurrentWindow.setVisible(true);

        if (mSessionStore == null) return;

        mMailboxController = MailboxController.getInstance();
        mMailboxController.setExceptionListener(this);

        mUsernameLabel.setText(Resource.getStringResourceWithParam("username", mUserName));
        mLogoutButton.addActionListener(e -> {
            mMailboxController.saveCache();
            LoginGUI.newInstance(mCurrentWindow);
        });

        mRefreshCacheButton.addActionListener(e -> mMailboxController.updateEmailFromAPI(mSessionStore));

        mCacheStatusLabel.setText(Resource.getStringResource(mMailboxController.isCached() ? "cachedMail" : "notCachedMail"));

        mMailboxComboBox.removeAllItems();
        List<String> mailFolders = mMailboxController.getFolderDisplayTitles();
        for(String mailFolder : mailFolders) {
            mMailboxComboBox.addItem(mailFolder);
        }
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
        mMessageContentTextArea.setText("");
        mMessageTitleComboBox.removeAllItems();
        List<String> mMessageTitleList = mMailboxController.getMessageTitles(folderDisplayName);
        for (String messageTitle : mMessageTitleList) {
            mMessageTitleComboBox.addItem(messageTitle);
        }
    }

    private void displayMessageContent(String messageSubject) {
        String[] tagAndMessage = mMailboxController.getMessageTagAndContent(messageSubject, mMailboxComboBox.getSelectedItem().toString());
        mMessageContentTextArea.setText(tagAndMessage[0]);
        mTagTextArea.setText(tagAndMessage[1]);
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
            case UNREADABLE_MESSAGE_CONTENT:
                showDialog(Resource.getStringResource("errorMessageContentTitle"), Resource.getStringResource("errorMessageContentMessage"));
                break;
            case FILE_IO_ERROR:
                showDialog(Resource.getStringResource("errorMessageFileIOTitle"), Resource.getStringResource("errorMessageFileIOMessage"));
                break;
        }
    }
}
