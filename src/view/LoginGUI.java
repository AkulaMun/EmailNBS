package view;

import controller.MailboxController;
import utils.Resource;

import javax.mail.Store;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JPanel;

/**
 * Created by Arcenal on 2/4/2016.
 */
public class LoginGUI extends GUI implements MailboxController.MailboxExceptionHandlerInterface{
    private JButton mLoginButton;
    private JTextField mNameInputTextField;
    private JPasswordField mPasswordInputPasswordField;
    private JButton mCancelButton;
    private JPanel mRootPanel;
    private LoginListener mLoginListener;
    private Store mSessionStore;

    public interface LoginListener {
        void onLogin(Store store);
    }

    private LoginGUI(JFrame frame) {
        mCurrentWindow = frame;
        initialize();
    }

    public static LoginGUI newInstance(JFrame frame) {
        return new LoginGUI(frame);
    }

    protected void initialize() {
        mCurrentWindow.setContentPane(mRootPanel);
        mCurrentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mCurrentWindow.pack();
        mCurrentWindow.setVisible(true);

        mLoginButton.addActionListener(e -> login());
        mCancelButton.addActionListener(e -> { mNameInputTextField.setText("");
            mPasswordInputPasswordField.setText("");});

    }

    private void login() {
        String username = mNameInputTextField.getText();
        String password = String.copyValueOf(mPasswordInputPasswordField.getPassword());

        if (username.length() == 0 || password.length() == 0) {
            showDialog(Resource.getStringResource("loginEmptyTitle"), Resource.getStringResource("loginEmptyMessage"));
            return;
        }

        MailboxController.getInstance().setExceptionListener(this);
        if ((mSessionStore = MailboxController.getInstance().login(username, password)) != null) {
            InboxGUI.newInstance(mCurrentWindow, mSessionStore, username);
        }
    }

    @Override
    public void onException(MailboxController.ExceptionType e) {
        if (e.equals(MailboxController.ExceptionType.SERVER_NOT_FOUND)) {
            showDialog(Resource.getStringResource("errorNoProviderTitle"), Resource.getStringResource("errorNoProviderMessage"));
        } else if (e.equals(MailboxController.ExceptionType.LOGIN_FAILURE)) {
            showDialog(Resource.getStringResource("errorLoginTitle"), Resource.getStringResource("errorLoginMessage"));
        }
    }
}