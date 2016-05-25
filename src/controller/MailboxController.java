package controller;

import javax.mail.*;
import java.io.IOException;
import java.util.*;

/**
 * Created by Arcenal on 2/4/2016.
 */
public class MailboxController {
    //TODO: method calls to this class is seriously slow.....Implementing multithreading??
    public interface MailboxExceptionHandlerInterface {
        void onException(ExceptionType e);
    }

    private final static String MAIL_PROTOCOL = "imaps";
    private final static String MAIL_URL = "imap.gmail.com";
    private final static int MAIL_PORT = 993;

    public enum ExceptionType {LOGIN_FAILURE, SERVER_NOT_FOUND, FOLDER_ACCESS_FAILURE, NO_MESSAGES, CORRUPT_MESSAGE_TITLE, UNREADABLE_MESSAGE_CONTENT}

    private static MailboxController sSelf;

    private MailboxExceptionHandlerInterface mExceptionListener;
    private List<Message> mMessageList = new ArrayList<>();
    private Map<String, String> mFolderNameMap = new HashMap<>();

    private MailboxController() {
    }

    public static MailboxController getInstance() {
        if (sSelf == null) {
            sSelf = new MailboxController();
        }
        return sSelf;
    }

    public void setExceptionListener(MailboxExceptionHandlerInterface exceptionListener) {
        mExceptionListener = exceptionListener;
    }

    public Store login(String username, String password) {
        Properties props = System.getProperties();
        Session session;
        Store store = null;

        props.setProperty("mail.store.protocol", MAIL_PROTOCOL);
        props.setProperty("mail.imap.ssl.enable", "true");
        session = Session.getInstance(props, null);
        session.setDebug(true);
        try {
            store = session.getStore(MAIL_PROTOCOL);
            store.connect(MAIL_URL, MAIL_PORT, username, password);
        } catch(Exception e) {
            if (mExceptionListener != null) {
                mExceptionListener.onException(ExceptionType.LOGIN_FAILURE);
            }
        }
        getStoreFolders(store);
        return store;
    }

    private void getStoreFolders(Store store) {
        if (store == null) return;

        try {
            Folder[] folders = store.getDefaultFolder().list("*");
            for (Folder folder : folders) {
                if((folder.getType() & Folder.HOLDS_MESSAGES) != 0){
                    String folderDisplayName = folder.getFullName();
                    folderDisplayName = folderDisplayName.replaceFirst("\\[Gmail\\]/", "");
                    mFolderNameMap.put(folderDisplayName, folder.getFullName());
                }
            }
        } catch (MessagingException e) {
            if (mExceptionListener != null) {
                mExceptionListener.onException(ExceptionType.FOLDER_ACCESS_FAILURE);
            }
        }
    }

    public Set<String> getFolderDisplayTitles() {
        if (!(mFolderNameMap.size() > 1)) {
            return null;
        } else {
            return mFolderNameMap.keySet();
        }
    }

    public String getFolderName(String displayName) {
        if (!(mFolderNameMap.size() > 1)) {
            return null;
        } else {
            return mFolderNameMap.get(displayName);
        }
    }

    public void updateMessageList(Store store, String folderName) {
        mMessageList.clear();
        Folder folder;
        try {
            folder = store.getFolder(folderName);
            folder.open(Folder.READ_ONLY);
            Message[] messageArray = folder.getMessages();
            for(Message message : messageArray) {
                mMessageList.add(message);
            }
        } catch (MessagingException e) {
            if (mExceptionListener != null) {
                mExceptionListener.onException(ExceptionType.NO_MESSAGES);
            }
        }
    }

    public List<String> getMessageTitles() {
        List<String> messageTitleList = new ArrayList<>();
        try {
            for (Message message : mMessageList) {
                messageTitleList.add(message.getSubject());
            }
        } catch (MessagingException e) {
            if (mExceptionListener != null) {
                mExceptionListener.onException(ExceptionType.CORRUPT_MESSAGE_TITLE);
            }
        }
        return messageTitleList;
    }

    public String getMessageContent(String messageSubject) {
        StringBuilder messageContentStringBuilder = new StringBuilder();
        try {
            for (Message message : mMessageList) {
                if (message.getSubject().equals(messageSubject)) {
                    if (message.getContent() instanceof String) {
                        messageContentStringBuilder.append(message.getContent());
                    } else {
                        Multipart messagePart = (Multipart) message.getContent();
                        for (int i = 0; i < messagePart.getCount(); i++) {
                            BodyPart messageBodyPart = messagePart.getBodyPart(i);
                            if (messageBodyPart.isMimeType("text/*")) {
                                messageContentStringBuilder.append(messageBodyPart.getContent());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (mExceptionListener != null) {
                mExceptionListener.onException(ExceptionType.UNREADABLE_MESSAGE_CONTENT);
                System.out.print(e.toString());
            }
        }
        return messageContentStringBuilder.toString();
    }
}