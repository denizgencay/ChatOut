package arsi.dev.chatout;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class App extends Application implements LifecycleObserver {

    public static boolean isForeground, chatChanging, deleteAccount;
    public static ArrayList<String> notificationBodies;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private boolean removedFromChat;
    private String userActiveChat;
    private ListenerRegistration task;

    @Override
    public void onCreate() {
        super.onCreate();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        removedFromChat = false;
        chatChanging = false;
        deleteAccount = false;
        notificationBodies = new ArrayList<>();
        userActiveChat = "";

        getActiveChatInfo();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        isForeground = true;
        if (removedFromChat) {
            Toast.makeText(App.this, "Maalesef bulunduğunuz konuşmadan çıkarıldınız", Toast.LENGTH_LONG).show();
            removedFromChat = false;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        isForeground = false;
    }

    private void getActiveChatInfo() {
        if (firebaseAuth.getCurrentUser() != null) {
            String uid = firebaseAuth.getCurrentUser().getUid();

            DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    String activeChat = documentSnapshot.getString("activeChat");

                    if (!activeChat.isEmpty()) {
                        if (deleteAccount) {
                            if (task != null) task.remove();
                            userActiveChat = "";
                            deleteAccount = false;
                            return;
                        } else if (chatChanging) {
                            if (task != null) task.remove();
                            chatChanging = false;
                            userActiveChat = activeChat;
                            return;
                        } else {
                            if (task != null) task.remove();
                            if (!activeChat.equals(userActiveChat)) {
                                userActiveChat = activeChat;
                                DocumentReference documentReference1 = firebaseFirestore.collection("chats").document(activeChat);
                                task = documentReference1.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                        ArrayList<String> peopleInChat = (ArrayList<String>) documentSnapshot.get("peopleInChat");

                                        if (peopleInChat != null) {
                                            if (!peopleInChat.contains(uid)) {
                                                if (isForeground)
                                                    Toast.makeText(App.this, "Maalesef bulunduğunuz konuşmadan çıkarıldınız", Toast.LENGTH_SHORT).show();
                                                else removedFromChat = true;
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        if (task != null) task.remove();
                        userActiveChat = "";
                    }
                    System.out.println(userActiveChat + " , " + activeChat);
                }
            });
        }
    }
}
