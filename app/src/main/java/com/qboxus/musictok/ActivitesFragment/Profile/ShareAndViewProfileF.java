package com.qboxus.musictok.ActivitesFragment.Profile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.qboxus.musictok.ActivitesFragment.Profile.InviteFriends.InviteFriendsFromContactsA;
import com.qboxus.musictok.Adapters.FollowingShareAdapter;
import com.qboxus.musictok.Adapters.ProfileSharingAdapter;
import com.qboxus.musictok.Adapters.VideoSharingAppsAdapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.Models.FollowingModel;
import com.qboxus.musictok.Models.ShareAppModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ShareAndViewProfileF extends BottomSheetDialogFragment implements View.OnClickListener {

    View view;
    Context context;
    FragmentCallBack callback;
    RecyclerView recyclerView;
    String userId;
    boolean fromSetting;
    TextView bottomBtn;
    String picUrl="";
    SimpleDraweeView userImage;

    public ShareAndViewProfileF() {
    }

    public ShareAndViewProfileF(String id,boolean fromSetting,String picUrl, FragmentCallBack callback) {
        userId = id;
        this.fromSetting=fromSetting;
        this.callback = callback;
        this.picUrl=picUrl;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_share_and_view_profile, container, false);
        context = view.getContext();

        userImage=view.findViewById(R.id.user_image);
        userImage.setOnClickListener(this);

        bottomBtn =view.findViewById(R.id.bottom_btn);
        bottomBtn.setOnClickListener(this);

        userImage.setController(Functions.frescoImageLoad(picUrl,userImage,false));

        if(Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN,false)) {
            getSharedApp();
        }

        return view;
    }


    ProfileSharingAdapter adapter;
    public void getSharedApp() {
        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        adapter = new ProfileSharingAdapter(context, getAppShareDataList(), new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                ShareAppModel item= (ShareAppModel) object;

                shareProfile(item);
            }
        });recyclerView.setAdapter(adapter);


    }

    public void shareProfile(ShareAppModel item) {
        String profielLink = Variables.http+"://"+getString(R.string.share_profile_domain_second)+getString(R.string.share_profile_endpoint_second) + Functions.getSharedPreference(getActivity()).getString(Variables.U_ID,"");
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.whatsapp)))
        {

            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.facebook)))
        {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
                sendIntent.setPackage("com.facebook.katana");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.messenger)))
        {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
                sendIntent.setPackage("com.facebook.orca");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.sms)))
        {
            try {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("sms_body",""+profielLink);
                startActivity(smsIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.copy_link)))
        {
            try {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", profielLink);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, context.getString(R.string.link_copy_in_clipboard), Toast.LENGTH_SHORT).show();
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.email)))
        {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
                sendIntent.setPackage("com.google.android.gm");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.other)))
        {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }

    }

    private ArrayList<ShareAppModel> getAppShareDataList() {
        ArrayList<ShareAppModel> dataList=new ArrayList<>();
        {
            if (Functions.appInstalledOrNot(view.getContext(),"com.whatsapp"))
            {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.whatsapp));
                item.setIcon(R.drawable.ic_share_whatsapp);
                dataList.add(item);
            }
        }
        {
            if (Functions.appInstalledOrNot(view.getContext(),"com.facebook.katana"))
            {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.facebook));
                item.setIcon(R.drawable.ic_share_facebook);
                dataList.add(item);
            }
        }
        {
            if (Functions.appInstalledOrNot(view.getContext(),"com.facebook.orca"))
            {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.messenger));
                item.setIcon(R.drawable.ic_share_messenger);
                dataList.add(item);
            }
        }
        {
            ShareAppModel item=new ShareAppModel();
            item.setName(getString(R.string.sms));
            item.setIcon(R.drawable.ic_share_sms);
            dataList.add(item);
        }
        {
            ShareAppModel item=new ShareAppModel();
            item.setName(getString(R.string.copy_link));
            item.setIcon(R.drawable.ic_share_copy_link);
            dataList.add(item);
        }
        {
            if (Functions.appInstalledOrNot(view.getContext(),"com.whatsapp"))
            {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.email));
                item.setIcon(R.drawable.ic_share_email);
                dataList.add(item);
            }
        }
        {
            ShareAppModel item=new ShareAppModel();
            item.setName(getString(R.string.other));
            item.setIcon(R.drawable.ic_share_other);
            dataList.add(item);
        }
        return dataList;
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.send_message_layout:
            {
                Bundle bundle = new Bundle();
                bundle.putString("action", "profileShareMessage");
                dismiss();

                if (callback != null)
                    callback.onResponce(bundle);
            }
            break;
            case R.id.user_image:
            {
                Intent intent=new Intent(view.getContext(), SeeFullImageA.class);
                intent.putExtra("image_url", picUrl);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
            break;
            case R.id.bottom_btn:
                dismiss();
                break;

        }


    }


}
