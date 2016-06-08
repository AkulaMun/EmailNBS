package controller;

import model.EmailCache;
import model.TaggedMessage;

import javax.mail.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by Arcenal on 2/4/2016.
 */
public class MailboxController {
    //TODO: API method call in this class is seriously slow.....Implementing multithreading??
    public interface MailboxExceptionHandlerInterface {
        void onException(ExceptionType e);
    }

    private final static String MAIL_PROTOCOL = "imaps";
    private final static String MAIL_URL = "imap.gmail.com";
    private final static int MAIL_PORT = 993;
    private final static String FILE_EXTENSION = ".emc";
    private final static String FILE_DIRECTORY = "Cache/";

    public enum ExceptionType {LOGIN_FAILURE, SERVER_NOT_FOUND, FOLDER_ACCESS_FAILURE, UNREADABLE_MESSAGE_CONTENT, FILE_IO_ERROR}

    private static MailboxController sSelf;

    private MailboxExceptionHandlerInterface mExceptionListener;
    private EmailCache mEmailCache;

    private String mUsername;
    private boolean mIsCachedEmail = false;

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

    //API call Method
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
            return null;
        }
        mUsername = username;
        Object readObject = readCacheFromFile("");
        if (readObject != null && readObject.getClass() == EmailCache.class) {
            EmailCache emailCache = (EmailCache) readObject;
            if (emailCache.matchUsername(username)) {
                mEmailCache = emailCache;
                mIsCachedEmail = true;
            }
        } else {
            mEmailCache = new EmailCache(mUsername);
            updateEmailFromAPI(store);
        }
        return store;
    }

    //API call Method
    public void updateEmailFromAPI(Store store) {
        if (store == null) return;
        mIsCachedEmail = false;
        mEmailCache.clearFolderMessages();
        try {
            Folder[] folders = store.getDefaultFolder().list("*");
            Map<String, String> folderDisplayNameToRealNameMap = new HashMap<>();
            for (Folder folder : folders) {
                if((folder.getType() & Folder.HOLDS_MESSAGES) != 0){
                    //Handling Email Folders
                    String folderDisplayName = folder.getFullName();
                    folderDisplayName = folderDisplayName.replaceFirst("\\[Gmail\\]/", "");
                    folderDisplayNameToRealNameMap.put(folderDisplayName, folder.getFullName());

                    //Handling Emails Within Folders
                    folder.open(Folder.READ_ONLY);
                    Message[] messageArray = folder.getMessages();
                    List<TaggedMessage> messageList = new ArrayList<>();
                    for(Message message : messageArray) {
                        TaggedMessage taggedMessage = new TaggedMessage(message);
                        messageList.add(taggedMessage);
                    }
                    mEmailCache.addFolderMessages(folder.getFullName(), messageList);
                }
            }
            mEmailCache.setDisplayNameToFolderNameMap(folderDisplayNameToRealNameMap);
        } catch (MessagingException e) {
            if (mExceptionListener != null) {
                mExceptionListener.onException(ExceptionType.FOLDER_ACCESS_FAILURE);
            }
        } catch (IOException e) {
            if (mExceptionListener != null) {
                mExceptionListener.onException(ExceptionType.UNREADABLE_MESSAGE_CONTENT);
            }
        }
    }

    public List<String> getFolderDisplayTitles() {
        return (mEmailCache.getAllFolderDisplayName().size() > 0) ? mEmailCache.getAllFolderDisplayName() : null;
    }

    public String getFolderName(String displayName) {
        return mEmailCache.getFolderName(displayName);
    }

    public List<String> getMessageTitles(String folderDisplayName) {
        List<String> messageTitleList = new ArrayList<>();
        for (TaggedMessage message : mEmailCache.getMessagesViaFolderDisplayName(folderDisplayName)) {
            messageTitleList.add(message.getSubject());
        }
        return messageTitleList;
    }

    public boolean isCached() {
        return mIsCachedEmail;
    }

    //First element is the message, the 2nd element is a string tag.
    public String[] getMessageTagAndContent(String messageSubject, String folderDisplayName) {
        String[] resultArray = new String[2];
        for (TaggedMessage message : mEmailCache.getMessagesViaFolderDisplayName(folderDisplayName)) {
            if (message.getSubject().contentEquals(messageSubject)) {
                resultArray[0] = message.getContent();
                resultArray[1] = message.getTag();
                return resultArray;
            }
        }
        return null;
    }

    public void saveCache() {
        writeCacheToFile(mEmailCache, "");
    }

    private void writeCacheToFile(Object savedObject, String directory) {
        String fileName = mUsername + FILE_EXTENSION;
        try {
            ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(directory + fileName)));
            writer.writeObject(savedObject);
            writer.close();
        } catch(Exception e) {
            System.out.print(e.toString());
            if(mExceptionListener != null) {
                mExceptionListener.onException(ExceptionType.FILE_IO_ERROR);
            }
        }
    }

    private Object readCacheFromFile(String directory) {
        String fileName = mUsername + FILE_EXTENSION;
        try {
            ObjectInputStream reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(directory + fileName)));
            Object readObject = reader.readObject();
            reader.close();
            return readObject;
        } catch(Exception e) {
            System.out.print(directory + fileName);
            if(mExceptionListener != null) {
                mExceptionListener.onException(ExceptionType.FILE_IO_ERROR);
            }
            return null;
        }
    }
}