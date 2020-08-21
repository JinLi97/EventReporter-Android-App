package laioffer.com;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMesagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    //called when message is received
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        //check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            if (true) {

            }else {
                handleNow();
            }
        }
        sendNotification("Send notification to start EventReporter");

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notifiaction Body: " + remoteMessage.getNotification().getBody());
        }
    }

    private void handleNow(){
        Log.d(TAG, "Short lived task is done.");
    }

    private void sendNotification(String fcmmessage) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "EventReporter");
        notificationBuilder.setSmallIcon(R.drawable.icon)
                .setContentTitle("FCM Message")
                .setContentText(fcmmessage)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
