package model;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Arcenal on 1/6/2016.
 */
public class TaggedMessage implements Serializable {
    private String mTag = "";
    private String mContent;
    private String mSubject;
    private Date mSentDate;
    private Date mReceivedDate;

    public TaggedMessage(Message message) throws MessagingException, IOException {
        mSubject = message.getSubject();
        mReceivedDate = message.getReceivedDate();
        mSentDate = message.getSentDate();
        setContent(message.getContent());
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(Object messageContent) throws MessagingException, IOException{
        StringBuilder messageContentStringBuilder = new StringBuilder();
        if (messageContent instanceof String) {
            messageContentStringBuilder.append(messageContent);
        } else {
            Multipart messagePart = (Multipart) messageContent;
            for (int i = 0; i < messagePart.getCount(); i++) {
                BodyPart messageBodyPart = messagePart.getBodyPart(i);
                if (messageBodyPart.isMimeType("text/*")) {
                    messageContentStringBuilder.append(messageBodyPart.getContent());
                }
            }
        }
        mContent = messageContentStringBuilder.toString();
    }

    public String getTag() {
        return mTag;
    }

    public List<String> getTagsAsList() {
        String[] tagArray = mTag.split(",");
        List<String> resultList = new ArrayList<>(tagArray.length);
        for (String tag : tagArray) {
            resultList.add(tag);
        }
        return resultList;
    }

    public void setTag(String newTag) {
        mTag = newTag;
    }

    public void addTag(String additionalTag) {
        if (mTag.length() == 0) {
            mTag = additionalTag;
        } else {
            mTag += "," + additionalTag;
        }
    }

    public String getSubject() {
        return mSubject;
    }

    public Date getSentDate() {
        return mSentDate;
    }

    public Date getReceivedDate() {
        return mReceivedDate;
    }
}
