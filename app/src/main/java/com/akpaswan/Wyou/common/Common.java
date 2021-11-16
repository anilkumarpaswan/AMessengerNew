package com.akpaswan.Wyou.common;

public class Common {
    public static final String CHAT_USER = "CHAT_USER";

    public static String getMessageId (String senderPhone, String receiverPhone) {
        if (senderPhone.compareTo (receiverPhone) < 0) {
            return (senderPhone + "_" + receiverPhone);
        } else {
            return (receiverPhone + "_" + senderPhone);
        }
    }
}
