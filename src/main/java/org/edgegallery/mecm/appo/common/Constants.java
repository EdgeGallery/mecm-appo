package org.edgegallery.mecm.appo.common;

public class Constants {

    public static final String HOST_IP_REGX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.)"
            + "{3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    public static final String APP_INST_ID_REGX
            = "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}";

    public static final String APP_PKG_ID_REGX = "[0-9a-f]{32}";
    public static final String TENENT_ID_REGEX = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    public static final String NOT_EXIST = "Record not exist";
}
