//package com.example.chatout;
//
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//
//import androidx.core.app.NotificationCompat;
//
//import com.onesignal.OSNotification;
//import com.onesignal.OneSignal;
//
//import java.util.Random;
//
//public class NotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
//    @Override
//    public void notificationReceived(OSNotification notification) {
//        System.out.println(notification.payload.additionalData);
////        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//////        String NOTIFICATION_CHANNEL_ID = remoteMessage.getData().get("key");
//////        String title = remoteMessage.getNotification().getTitle();
//////        String body = remoteMessage.getNotification().getBody();
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////            NotificationChannel notificationChannel = new NotificationChannel("NOTIFICATION_CHANNEL_ID","Notification",NotificationManager.IMPORTANCE_DEFAULT);
////
//////            notificationChannel.setDescription(remoteMessage.getData().get("key"));
////            notificationChannel.enableVibration(true);
////            notificationChannel.enableLights(false);
////            notificationManager.createNotificationChannel(notificationChannel);
////        }
////        Intent intent = new Intent();
////        switch (NOTIFICATION_CHANNEL_ID){
////            case "friendRequest":
////                intent = new Intent(this,MainPage.class);
////                intent.putExtra("key",remoteMessage.getData().get("key"));
////                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
////                break;
////        }
////        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
////        NotificationCompat.Builder builder = new NotificationCompat.Builder(,NOTIFICATION_CHANNEL_ID);
////
////        builder.setAutoCancel(true)
////                .setDefaults(Notification.DEFAULT_ALL)
////                .setWhen(System.currentTimeMillis())
////                .setSmallIcon(R.drawable.ic_wifi_black_24dp)
////                .setContentTitle(title)
////                .setContentText(body)
////                .setContentInfo("Info")
////                .setContentIntent(pendingIntent);
////
////
////        notificationManager.notify(new Random().nextInt(),builder.build());
//    }
//}
