package model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Arcenal on 25/5/2016.
 */
public class EmailCache implements Serializable {
    private String mUsername;
    private String mMailboxHost;
    private Map<String, List<TaggedMessage>> mFolderMessageMap = new HashMap<>();
    private Map<String, String> mDisplayNameToFolderNameMap = new HashMap<>();

    public EmailCache(String username) {
        mUsername = username;
    }

    public EmailCache(String username, String mailboxHost) {
        mUsername = username;
        mMailboxHost = mailboxHost;
    }

    public void clearFolderMessages() {
        mFolderMessageMap.clear();
    }

    public void addFolderMessages(String folderName, List<TaggedMessage> messages) {
        mFolderMessageMap.put(folderName, messages);
    }

    public List<TaggedMessage> getMessagesViaFolderDisplayName(String folderDisplayName) {
        return mFolderMessageMap.get(getFolderName(folderDisplayName));
    }

    public List<TaggedMessage> getMessagesViaFolderName(String folderName) {
        return mFolderMessageMap.get(folderName);
    }

    public List<TaggedMessage> getAllMessages() {
        List<TaggedMessage> allMessagesList = new ArrayList<>();
        for (String folder : mFolderMessageMap.keySet()) {
            allMessagesList.addAll(mFolderMessageMap.get(folder));
        }
        return allMessagesList;
    }

    public void setDisplayNameToFolderNameMap(Map<String, String> folderNameMap) {
        mDisplayNameToFolderNameMap = folderNameMap;
    }

    public boolean matchUsername(String username) {
        return mUsername.contentEquals(username);
    }

    public List<String> getAllFolderDisplayName() {
        Set<String> folderDisplayNameSet = mDisplayNameToFolderNameMap.keySet();
        return new ArrayList<>(folderDisplayNameSet);
    }

    public String getFolderName(String displayName) {
        return mDisplayNameToFolderNameMap.get(displayName);
    }
}
