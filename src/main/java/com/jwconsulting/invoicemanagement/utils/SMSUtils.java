package com.jwconsulting.invoicemanagement.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import static com.twilio.rest.api.v2010.account.Message.creator;

public class SMSUtils {
    public static final String FROM_NUMBER = "+18669513570";
    public static final String SID_KEY = "AC8098c5fae31cbb45b9fa9d80c8b26e18";
    public static final String TOKEN_KEY = "";
    public static void sendSMS(String to, String messageBody) {
        Twilio.init(SID_KEY, TOKEN_KEY);
        Message message = creator(new PhoneNumber("+" + to), new PhoneNumber(FROM_NUMBER), messageBody).create();
        System.out.println("Twilio Text Message: " + message);
    }
}
