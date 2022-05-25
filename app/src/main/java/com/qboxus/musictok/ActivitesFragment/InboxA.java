package com.qboxus.musictok.ActivitesFragment;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.qboxus.musictok.ActivitesFragment.Chat.ChatA;
import com.qboxus.musictok.ActivitesFragment.Profile.Setting.NoInternetA;
import com.qboxus.musictok.ActivitesFragment.Search.SearchMainA;
import com.qboxus.musictok.Adapters.InboxAdapter;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.Interfaces.InternetCheckCallback;
import com.qboxus.musictok.Models.InboxModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.util.ArrayList;
import java.util.Collections;

public class InboxA extends AppCompatLocaleActivity {

    Context context;
    RecyclerView inboxList;
    ArrayList<InboxModel> inboxArraylist;
    DatabaseReference rootRef;
    InboxAdapter inboxAdapter;
    ProgressBar pbar;
    boolean isviewCreated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(InboxA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, InboxA.class,false);
        setContentView(R.layout.activity_inbox);
        context = InboxA.this;

        rootRef = FirebaseDatabase.getInstance().getReference();


        pbar = findViewById(R.id.pbar);
        inboxList = findViewById(R.id.inboxlist);

        // intialize the arraylist and and inboxlist
        inboxArraylist = new ArrayList<>();

        inboxList = (RecyclerView) findViewById(R.id.inboxlist);
        LinearLayoutManager layout = new LinearLayoutManager(context);
        inboxList.setLayoutManager(layout);
        inboxList.setHasFixedSize(false);
        inboxAdapter = new InboxAdapter(context, inboxArraylist, new InboxAdapter.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(InboxModel item) {
                chatFragment(item.getId(), item.getName(), item.getPic());
            }
        }, new InboxAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(InboxModel item) {

            }
        });

        inboxList.setAdapter(inboxAdapter);



       findViewById(R.id.back_btn).setOnClickListener(v -> {
           InboxA.super.onBackPressed();


        });
        isviewCreated = true;
        getData();

    }

    // show the banner ad at the bottom of the screen
    AdView adView;

    @Override
    public void onStart() {
        super.onStart();
        adView = findViewById(R.id.bannerad);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }


    // on start we will get the Inbox Message of user  which is show in bottom list of third tab
    ValueEventListener eventListener2;
    Query inboxQuery;

    public void getData() {

        pbar.setVisibility(View.VISIBLE);

        inboxQuery = rootRef.child("Inbox").child(Functions.getSharedPreference(InboxA.this).getString(Variables.U_ID, "0")).orderByChild("date");
        eventListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                inboxArraylist.clear();
                pbar.setVisibility(View.GONE);
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    InboxModel model = ds.getValue(InboxModel.class);
                    model.setId(ds.getKey());

                    inboxArraylist.add(model);
                }


                if (inboxArraylist.isEmpty()) {
                    Functions.showToast(context, getString(R.string.no_data));
                    findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
                } else {
                    Functions.showToast(context,  getString(R.string.no_data));
                    findViewById(R.id.no_data_layout).setVisibility(View.GONE);
                    Collections.reverse(inboxArraylist);
                    inboxAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                pbar.setVisibility(View.GONE);
               findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            }
        };

        inboxQuery.addValueEventListener(eventListener2);


    }


    // on stop we will remove the listener
    @Override
    public void onStop() {
        super.onStop();
        if (inboxQuery != null)
            inboxQuery.removeEventListener(eventListener2);
    }


    //open the chat fragment and on item click and pass your id and the other person id in which
    //you want to chat with them and this parameter is that is we move from match list or inbox list
    public void chatFragment(String receiverid, String name, String picture) {
        Intent intent=new Intent(InboxA.this,ChatA.class);
        intent.putExtra("user_id", receiverid);
        intent.putExtra("user_name", name);
        intent.putExtra("user_pic", picture);
        resultCallback.launch(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }


    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false))
                        {

                        }
                    }
                }
            });



}
