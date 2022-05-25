package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.qboxus.musictok.Adapters.SwitchAccountAdapter;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.Models.MultipleAccountModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.util.ArrayList;

import io.paperdb.Paper;


public class ManageAccountsF extends BottomSheetDialogFragment implements View.OnClickListener {

    private View view;
    private RecyclerView recyclerView;
    FragmentCallBack callback;
    ImageView ivClose;
    LinearLayout tabAddAccount;
    private SwitchAccountAdapter adapter;
    private ArrayList<MultipleAccountModel> list = new ArrayList<>();

    public ManageAccountsF() {
    }

    public ManageAccountsF( FragmentCallBack callback) {
        this.callback = callback;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       view=inflater.inflate(R.layout.fragment_manage_accounts, container, false);
       return init();
    }

    private View init() {
        ivClose=view.findViewById(R.id.ivClose);
        ivClose.setOnClickListener(this);

        tabAddAccount=view.findViewById(R.id.tabAddAccount);
        tabAddAccount.setOnClickListener(this);

        recyclerView=view.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new SwitchAccountAdapter(list, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                MultipleAccountModel item= (MultipleAccountModel) object;
                if (item.isCheck())
                {
                    // nothing to do because we are already login
                }
                else
                {
                    Functions.setUpNewSelectedAccount(view.getContext(),item);
                }
            }
        });
        recyclerView.setAdapter(adapter);

        Functions.setUpMultipleAccount(view.getContext());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                getAccountList();
            }
        },300);
        return view;
    }

    private void getAccountList() {
        list.clear();
        {
            for (String key: Paper.book(Variables.MultiAccountKey).getAllKeys())
            {
                MultipleAccountModel item=Paper.book(Variables.MultiAccountKey).read(key);
                if (item.getId().equalsIgnoreCase(Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID, "")))
                {
                    item.setCheck(true);
                }
                else
                {
                    item.setCheck(false);
                }

                list.add(item);
                adapter.notifyDataSetChanged();
            }

        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ivClose:
               dismiss();
                break;
            case R.id.tabAddAccount:
                openAddNewAccount();
                break;
        }
    }

    private void openAddNewAccount() {
        Bundle bundle=new Bundle();
        bundle.putBoolean("isShow",true);
        callback.onResponce(bundle);
        dismiss();
    }

}