package utils;

import java.util.ResourceBundle;

/**
 * Created by Arcenal on 2/4/2016.
 */
public class Resource {
    public static String getStringResource(String key) {
        ResourceBundle stringResource = ResourceBundle.getBundle("strings");
        return stringResource.getString(key) != null ? stringResource.getString(key) : "";
    }

    public static String getStringResourceWithParam(String key, String stringParam) {
        String resString = getStringResource(key);
        return resString.replace("PARAM", stringParam);
    }
}