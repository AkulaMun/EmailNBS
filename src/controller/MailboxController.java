package controller;

import javax.mail.*;
import java.util.*;

/**
 * Created by Arcenal on 2/4/2016.
 */
public class MailboxController {
    public interface MailboxExceptionHandlerInterface {
        void onException(ExceptionType e);
    }

    public enum MailFolder {ALL, INBOX, DRAFT, STARRED, SENT, SPAM, TRASH}
    public enum ExceptionType {LOGIN_FAILURE, SERVER_NOT_FOUND, NO_MESSAGES}

    private MailboxExceptionHandlerInterface mExceptionListener;
    private Map<MailFolder, List<Message>> mFolderMessageMap = new HashMap<>();
    private List<Message> mMessageList = new ArrayList<>();

    private MailboxController(MailboxExceptionHandlerInterface exceptionListener) {
        mExceptionListener = exceptionListener;
    }

    public static MailboxController newInstance(MailboxExceptionHandlerInterface exceptionListener) {
        return new MailboxController(exceptionListener);
    }

    public Store login(String username, String password) {
        Properties props = System.getProperties();
        Session session;
        Store store = null;

        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imap.ssl.enable", "true");
        session = Session.getInstance(props, null);
        session.setDebug(true);
        try {
            store = session.getStore("imaps");
            store.connect("imap.gmail.com", 993, username, password);
        } catch(NoSuchProviderException e) {
            if (mExceptionListener != null) {
                mExceptionListener.onException(ExceptionType.LOGIN_FAILURE);
            }
        } catch(MessagingException e) {
            if (mExceptionListener != null) {
                mExceptionListener.onException(ExceptionType.SERVER_NOT_FOUND);
            }
        }
        return store;
    }

    public List<Message> getMessages(Store store, MailFolder folderType) {
        List<Message> messageList = new ArrayList<>();
        String folderName;
        Folder folder;
        switch (folderType) {
            case ALL:
                folderName = "All";
                break;
            case INBOX:
                folderName = "Inbox";
                break;
            case STARRED:
                folderName = "Starred";
                break;
            case DRAFT:
                folderName = "Draft";
                break;
            case SENT:
                folderName = "Sent";
                break;
            case SPAM:
                folderName = "Spam";
                break;
            case TRASH:
                folderName = "Trash";
                break;
            default:
                folderName = "All";
                break;
        }
        try {
            folder = store.getFolder(folderName);
            folder.open(Folder.READ_ONLY);
            Message[] messageArray = folder.getMessages();

            for(Message message : messageArray) {
                messageList.add(message);
            }
        } catch (MessagingException e) {
            if (mExceptionListener != null) {
                mExceptionListener.onException(ExceptionType.NO_MESSAGES);
            }
            return null;
        }
        return messageList;
        //Add into map

    }
}