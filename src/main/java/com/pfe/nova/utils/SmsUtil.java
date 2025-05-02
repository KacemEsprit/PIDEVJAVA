package com.pfe.nova.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsUtil {
    // Find your Account Sid and Token at twilio.com/console
    public static final String ACCOUNT_SID = "AC24f1acdf0cdcac020984aa55758cad52";
    public static final String AUTH_TOKEN = "a6d0fc00603691ed55077f81a0065e7b";
    public static final String FROM_PHONE = "+16203198935"; // Replace with your Twilio phone number

    public static void sendSMS(String messagestring) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        try {
            Message message = Message.creator(
                    new PhoneNumber("+21696602940"), // Recipient's phone number
                    new PhoneNumber(FROM_PHONE),    // Sender's phone number (Twilio number)
                    messagestring  // Message body
            ).create();

            System.out.println("Message sent successfully! SID: " + message.getSid());
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }


}