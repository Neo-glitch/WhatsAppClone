package com.neo.whatsappclone.Utils;


import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class handles the sending of Notification using the OneSignal platform
 */
public class SendNotification {

    public SendNotification(String message, String heading, String notificationKey) {
        try {
            JSONObject notificationContent = new JSONObject(                                    // jsonObj with needed info of what to send and who to send to
                    "{'contents':{'en':'" + message + "'}," +
                    "'include_player_ids':['" + notificationKey + "']," +
                    "'headings':{'en': '" + heading + "'}}");

            OneSignal.postNotification(notificationContent, null);
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }
}
