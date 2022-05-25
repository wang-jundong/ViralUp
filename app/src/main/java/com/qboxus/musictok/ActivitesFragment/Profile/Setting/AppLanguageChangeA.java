package com.qboxus.musictok.ActivitesFragment.Profile.Setting;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qboxus.musictok.ActivitesFragment.SplashA;
import com.qboxus.musictok.Adapters.LanguageAdapter;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.Models.LanguageModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.util.ArrayList;

public class AppLanguageChangeA extends AppCompatLocaleActivity implements View.OnClickListener {

    LanguageModel selectedLanguage;
    EditText etSearch;
    ProgressBar progressBar;
    TextView noData;
    ArrayList<LanguageModel> languageList=new ArrayList<>();
    RecyclerView recyclerview;
    LanguageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(AppLanguageChangeA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, AppLanguageChangeA.class,false);
        setContentView(R.layout.activity_app_language_change);

        InitControl();
    }

    private void InitControl() {
        findViewById(R.id.tvCancel).setOnClickListener(this);
        findViewById(R.id.tvDone).setOnClickListener(this);
        progressBar=findViewById(R.id.progressBar);
        recyclerview=findViewById(R.id.recyclerview);
        noData=findViewById(R.id.noData);
        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FilterList(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setUpAdapter();
    }

    private void setUpAdapter() {
        String localeName[]=getResources().getStringArray(R.array.app_language);
        String localeKey[]=getResources().getStringArray(R.array.app_language_code);

        languageList.clear();
        progressBar.setVisibility(View.VISIBLE);
        for (int i=0;i<localeName.length;i++)
        {
            LanguageModel model=new LanguageModel();
            if (Functions.getSharedPreference(AppLanguageChangeA.this)
                    .getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE).equalsIgnoreCase(localeKey[i]))
            {
                model.setSelected(true);
            }
            else
            {
                model.setSelected(false);
            }
            model.setName(localeName[i]);
            model.setKey(localeKey[i]);
            languageList.add(model);
        }
        progressBar.setVisibility(View.GONE);


        LinearLayoutManager layoutManager=new LinearLayoutManager(AppLanguageChangeA.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerview.setLayoutManager(layoutManager);
        adapter=new LanguageAdapter(languageList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                LanguageModel model= (LanguageModel) object;

                for (int i=0;i<languageList.size();i++)
                {
                    LanguageModel modelUpdate=languageList.get(i);
                    if (model.getName().equalsIgnoreCase(modelUpdate.getName()))
                    {
                        modelUpdate.setSelected(true);
                    }
                    else
                    {
                        modelUpdate.setSelected(false);
                    }

                    languageList.set(i,modelUpdate);
                }
                adapter.notifyDataSetChanged();
            }
        });
        recyclerview.setAdapter(adapter);

        if (languageList.size()>0)
        {
            recyclerview.setVisibility(View.VISIBLE);
            noData.setVisibility(View.GONE);
        }
        else
        {
            recyclerview.setVisibility(View.GONE);
            noData.setVisibility(View.VISIBLE);
        }

    }

    private void FilterList(CharSequence s) {
        try {
            ArrayList<LanguageModel> filter_list=new ArrayList<>();
            for (LanguageModel model:languageList)
            {
                if (model.getName().toLowerCase().contains(s.toString().toLowerCase()))
                {
                    filter_list.add(model);
                }
            }

            if (filter_list.size()>0)
            {
                adapter.filter(filter_list);
            }

        }
        catch (Exception e)
        {
            Log.d(Constants.tag,"Exception : "+e);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.tvCancel:
            {
                AppLanguageChangeA.super.onBackPressed();
            }
                break;
            case R.id.tvDone:
            {
                boolean isAnyLanSelect=false;
                for (LanguageModel model:languageList)
                {
                    if (model.isSelected())
                    {
                        selectedLanguage=model;
                        isAnyLanSelect=true;
                    }
                }
                if (!isAnyLanSelect)
                {
                    Toast.makeText(this, getString(R.string.must_select_any_language), Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences.Editor editor2 = Functions.getSharedPreference(AppLanguageChangeA.this).edit();
                editor2.putString(Variables.APP_LANGUAGE, selectedLanguage.getName());
                editor2.putString(Variables.APP_LANGUAGE_CODE, selectedLanguage.getKey());
                editor2.commit();

                Functions.setLocale(Functions.getSharedPreference(AppLanguageChangeA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                        , this, SplashA.class,true);

            }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        Functions.hideSoftKeyboard(AppLanguageChangeA.this);
        super.onDestroy();
    }
}