package com.qboxus.musictok.ActivitesFragment.Profile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.qboxus.musictok.ActivitesFragment.Profile.InviteFriends.InviteFriendsFromContactsA;
import com.qboxus.musictok.Adapters.ProfileSharingAdapter;
import com.qboxus.musictok.Adapters.VideoSharingAppsAdapter;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.Interfaces.ShareIntentCallback;
import com.qboxus.musictok.Models.ShareAppModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShareItemViaIntentA extends BottomSheetDialogFragment implements View.OnClickListener  {

    View view;
    ProfileSharingAdapter adapter;
    RecyclerView recyclerView;
    ShareIntentCallback callback;


    public ShareItemViaIntentA(ShareIntentCallback callback) {
        this.callback = callback;
    }

    public ShareItemViaIntentA() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_share_item_via_intent_a, container, false);
        InitControl();
        return view;
    }

    private void InitControl() {
        view.findViewById(R.id.bottom_btn).setOnClickListener(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        adapter = new ProfileSharingAdapter(view.getContext(), getAppShareDataList(), new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                ShareAppModel item= (ShareAppModel) object;

                shareProfile(item);
            }
        });
        recyclerView.setAdapter(adapter);
    }



    public void shareProfile(ShareAppModel item) {
        File file = new File(Functions.getAppFolder(getActivity())+"Screenshots","QrScreenshot.png");
        Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".fileprovider", file);

        String profielLink = Variables.http+"://"+getString(R.string.share_profile_domain_second)+getString(R.string.share_profile_endpoint_second) + Functions.getSharedPreference(getActivity()).getString(Variables.U_ID,"");
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.whatsapp)))
        {

            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
                sendIntent .putExtra(Intent.EXTRA_STREAM, uri);
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
                sendIntent .putExtra(Intent.EXTRA_STREAM, uri);
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
                sendIntent .putExtra(Intent.EXTRA_STREAM, uri);
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
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", profielLink);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(view.getContext(), view.getContext().getString(R.string.link_copy_in_clipboard), Toast.LENGTH_SHORT).show();
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
                sendIntent .putExtra(Intent.EXTRA_STREAM, uri);
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
                sendIntent .putExtra(Intent.EXTRA_STREAM, uri);
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
        switch (view.getId())
        {
            case R.id.bottom_btn:
            {
                dismiss();
            }
            break;
        }
    }
}