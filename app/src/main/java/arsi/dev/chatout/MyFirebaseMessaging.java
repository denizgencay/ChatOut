package arsi.dev.chatout;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    public static boolean inOthersProfile = false, inChat = false;
    public static String inOthersProfileUserId = "";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        if (firebaseUser != null) {
            updateToken(refreshToken);
        }
    }

    private void updateToken(String refreshToken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid());

        HashMap<String, Object> update = new HashMap<>();
        update.put("pushToken", refreshToken);

        documentReference.set(update, SetOptions.merge());
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().get("key").equals("friendRequest") || remoteMessage.getData().get("key").equals("createChat")) {
            if (!inOthersProfile) showNotification(remoteMessage);
            else if (!inOthersProfileUserId.equals(remoteMessage.getData().get("userId"))) showNotification(remoteMessage);
        } else if (remoteMessage.getData().get("key").equals("newMessage")) {
            if (!inChat) {
                App.notificationBodies.add(remoteMessage.getData().get("body"));
                showNotification(remoteMessage);
            }
        }
    }

    private void showNotification(RemoteMessage remoteMessage) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID;
        if (App.isForeground) NOTIFICATION_CHANNEL_ID = "FOREGROUND_NOTIFICATIONS";
        else  NOTIFICATION_CHANNEL_ID = "BACKGROUND_NOTIFICATIONS";

        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("FOREGROUND_NOTIFICATIONS","FOREGROUND",NotificationManager.IMPORTANCE_HIGH);
            NotificationChannel notificationChannel1 = new NotificationChannel("BACKGROUND_NOTIFICATIONS","BACKGROUND", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("FOREGROUND");
            notificationChannel.enableVibration(true);
            notificationChannel.enableLights(false);
            notificationChannel.setSound(null,null);
            notificationChannel1.setDescription("BACKGROUND");
            notificationChannel1.enableVibration(true);
            notificationChannel1.enableLights(false);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.createNotificationChannel(notificationChannel1);
        }

        Intent intent = new Intent();
        switch (remoteMessage.getData().get("key")) {
            case "friendRequest":
            case "createChat":
                intent = new Intent(this, MainPage.class);
                intent.putExtra("key", remoteMessage.getData().get("key"));
                intent.putExtra("userId", remoteMessage.getData().get("userId"));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case "newMessage":
                intent = new Intent(this, MainPage.class);
                intent.putExtra("key", remoteMessage.getData().get("key"));
                intent.putExtra("chatId", remoteMessage.getData().get("chatId"));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        NotificationCompat.Builder summary = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        if (App.notificationBodies.size() > 5) {
            for (int i = 1; i < 6; i++) {
                inboxStyle.addLine(App.notificationBodies.get(App.notificationBodies.size() - i));
            }
        } else {
            for (int i = 0; i < App.notificationBodies.size(); i++) {
                inboxStyle.addLine(App.notificationBodies.get(i));
            }
        }

        if (!remoteMessage.getData().get("key").equals("newMessage")) {
            Bundle bundle = new Bundle();
            bundle.putString("userId",remoteMessage.getData().get("userId"));
            builder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentInfo("Info")
                    .setExtras(bundle)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent);

            notificationManager.notify(1, builder.build());
        } else {
            if (App.isForeground) {
                summary.setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentInfo("Info")
                        .setGroup(title)
                        .setGroupSummary(true)
                        .setStyle(inboxStyle.setSummaryText(App.notificationBodies.size() + " yeni mesaj."))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);
            } else {
                summary.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                        .setStyle(inboxStyle.setSummaryText(App.notificationBodies.size() + " yeni mesaj."))
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentInfo("Info")
                        .setGroup(title)
                        .setGroupSummary(true)
                        .setOnlyAlertOnce(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent);
            }
            notificationManager.notify(0, summary.build());
        }
    }
}
