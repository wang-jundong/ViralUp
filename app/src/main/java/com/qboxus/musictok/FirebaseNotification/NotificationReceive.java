package com.qboxus.musictok.FirebaseNotification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.ActivitesFragment.Chat.ChatA;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.activities.LiveActivity;
import com.qboxus.musictok.ActivitesFragment.Profile.ProfileA;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.MainMenu.MainMenuActivity;
import com.qboxus.musictok.MainMenu.MainMenuFragment;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * Created by qboxus on 5/22/2018.
 */

public class NotificationReceive extends FirebaseMessagingService {

    SharedPreferences sharedPreferences;
    String pic;
    String title;
    String userId;
    String message;
    String senderid;
    String receiverid;
    String action_type;

    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable;

    Snackbar snackbar;


    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            sharedPreferences = getSharedPreferences(Variables.PREF_NAME, MODE_PRIVATE);
            title = remoteMessage.getData().get("title");
            if (title != null)
                title.replaceAll("@", "");

            message = remoteMessage.getData().get("body");

            try {
                // its for multiple account notification handling
                Log.d(Constants.tag,"Notification : "+remoteMessage.getData());
                userId=remoteMessage.getData().get("receiver_id");
            }catch (Exception e){userId="";}


            pic = remoteMessage.getData().get("image");
            if (pic != null && !pic.contains(Variables.http))
                pic = Constants.BASE_URL + pic;

            senderid = remoteMessage.getData().get("user_id");
            receiverid = sharedPreferences.getString(Variables.U_ID, "");
            action_type = remoteMessage.getData().get("type");

            Functions.printLog(Constants.tag, action_type);
            JSONObject json = new JSONObject();
            Set<String> keys = remoteMessage.getData().keySet();
            for (String key : keys) {
                try {
                    json.put(key, JSONObject.wrap(remoteMessage.getData().get(key)));
                    Functions.printLog(Constants.tag, json.toString());
                } catch (Exception e) {

                }
            }

            if (!ChatA.senderidForCheckNotification.equals(senderid)) {

                SendNotification sendNotification = new SendNotification(this);
                sendNotification.execute(pic);

            }

        }
    }


    // this will store the user firebase token in local storage
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        sharedPreferences = getSharedPreferences(Variables.PREF_NAME, MODE_PRIVATE);

        if (s == null) {

        } else if (s.equals("null")) {

        } else if (s.equals("")) {

        } else if (s.length() < 6) {

        } else {
            sharedPreferences.edit().putString(Variables.DEVICE_TOKEN, s).commit();
        }

    }

    private class SendNotification extends AsyncTask<String, Void, Bitmap> {

        Context ctx;

        public SendNotification(Context context) {
            super();
            this.ctx = context;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            // in notification first we will get the image of the user and then we will show the notification to user
            // in onPostExecute
            InputStream in;
            try {

                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @SuppressLint("WrongConstant")
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            // its for multiple account notification handling
            if (Functions.getSharedPreference(getApplicationContext()).getString(Variables.U_ID,"").equalsIgnoreCase(userId))
            {

                showNotification(ctx, title, message, result);

                if (MainMenuActivity.mainMenuActivity != null) {


                    if (snackbar != null) {
                        snackbar.getView().setVisibility(View.INVISIBLE);
                        snackbar.dismiss();
                    }

                    if (handler != null && runnable != null) {
                        handler.removeCallbacks(runnable);
                    }


                    View layout = MainMenuActivity.mainMenuActivity.getLayoutInflater().inflate(R.layout.item_layout_custom_notification, null);
                    TextView titletxt = layout.findViewById(R.id.username);
                    TextView messagetxt = layout.findViewById(R.id.message);
                    SimpleDraweeView imageView = layout.findViewById(R.id.user_image);
                    titletxt.setText(title);
                    messagetxt.setText(message);

                    if (result != null)
                    {
                       File file=Functions.getBitmapToUri(getApplicationContext(),result,"userTemp.jpg");
                        imageView.setController(Functions.frescoImageLoad(Uri.fromFile(file),false));
                    }

                    snackbar = Snackbar.make(MainMenuActivity.mainMenuActivity.findViewById(R.id.container), "", Snackbar.LENGTH_LONG);

                    Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
                    TextView textView = (TextView) snackbarLayout.findViewById(R.id.snackbar_text);
                    textView.setVisibility(View.INVISIBLE);

                    final ViewGroup.LayoutParams params = snackbar.getView().getLayoutParams();
                    if (params instanceof CoordinatorLayout.LayoutParams) {
                        ((CoordinatorLayout.LayoutParams) params).gravity = Gravity.TOP;
                    } else {
                        ((FrameLayout.LayoutParams) params).gravity = Gravity.TOP;
                    }

                    snackbarLayout.setPadding(0, 0, 0, 0);
                    snackbarLayout.addView(layout, 0);


                    snackbar.getView().setVisibility(View.INVISIBLE);

                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar sb) {
                            super.onShown(sb);
                            snackbar.getView().setVisibility(View.VISIBLE);
                        }

                    });


                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            snackbar.getView().setVisibility(View.INVISIBLE);

                        }
                    };

                    handler.postDelayed(runnable, 2750);


                    snackbar.setDuration(Snackbar.LENGTH_LONG);
                    snackbar.show();


                    layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            snackbar.dismiss();
                            snackbar.getView().setVisibility(View.INVISIBLE);

                            if (action_type.equals("message"))
                                chatFragment(senderid, title, pic);

                        }
                    });


                }

            }


        }

    }


    // create the local notification when any kind of notification is receive
    public void showNotification(Context context, String title, String message, Bitmap bitmap) {

        // The id of the channel.
        final String CHANNEL_ID = "default";
        final String CHANNEL_NAME = "Default";

        Intent notificationIntent;

        if (action_type != null && action_type.equals("live")) {
            notificationIntent = new Intent(context, LiveActivity.class);
            notificationIntent.putExtra("user_id", senderid);
            notificationIntent.putExtra("user_name", title);
            notificationIntent.putExtra("user_picture", pic);
            Functions.printLog(Constants.tag, action_type);
            notificationIntent.putExtra("user_role", 2);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        else {
            notificationIntent = new Intent(context, MainMenuActivity.class);
            notificationIntent.putExtra("user_id", senderid);
            notificationIntent.putExtra("user_name", title);
            notificationIntent.putExtra("user_pic", pic);
            notificationIntent.putExtra("message", message);
            Functions.printLog(Constants.tag, action_type);
            notificationIntent.putExtra("type", action_type);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);

        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel defaultChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(defaultChannel);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(title))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setLargeIcon(bitmap)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(100, notification);
    }

    public void chatFragment(String senderid, String name, String picture) {

        if (sharedPreferences.getBoolean(Variables.IS_LOGIN, false)) {

            if (MainMenuFragment.tabLayout != null) {
                TabLayout.Tab tab3 = MainMenuFragment.tabLayout.getTabAt(3);
                tab3.select();
            }

            Intent intent=new Intent(getApplicationContext(),ChatA.class);
            intent.putExtra("user_id", senderid);
            intent.putExtra("user_name", name);
            intent.putExtra("user_pic", picture);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }


    }


}
