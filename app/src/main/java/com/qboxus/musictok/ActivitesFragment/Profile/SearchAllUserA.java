package com.qboxus.musictok.ActivitesFragment.Profile;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.qboxus.musictok.Adapters.RecentSearchAdapter;
import com.qboxus.musictok.Adapters.UsersAdapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.Models.UsersModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import io.paperdb.Paper;

public class SearchAllUserA extends AppCompatLocaleActivity implements View.OnClickListener{

    EditText searchEdit;
    TextView search_btn;
    ShimmerFrameLayout shimmerFrameLayout;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RelativeLayout noDataLayout;
    ProgressBar loadMoreProgress;
    int pageCount = 0;
    boolean ispostFinsh;
    ArrayList<UsersModel> dataList;
    UsersAdapter usersAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(SearchAllUserA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, SearchAllUserA.class,false);
        setContentView(R.layout.activity_search_all_user);

        InitControl();
    }

    private void InitControl() {
        findViewById(R.id.ivBack).setOnClickListener(this);
        searchEdit = findViewById(R.id.search_edit);
        search_btn = findViewById(R.id.search_btn);
        search_btn.setOnClickListener(this);
        showRecentSearch();

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (searchEdit.getText().toString().length() > 0) {
                    search_btn.setVisibility(View.VISIBLE);

                } else {
                    search_btn.setVisibility(View.GONE);
                }

                showRecentSearch();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchEdit.setFocusable(true);
        UIUtil.showKeyboard(SearchAllUserA.this, searchEdit);


        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    pageCount = 0;
                    shimmerFrameLayout.setVisibility(View.VISIBLE);
                    shimmerFrameLayout.startShimmer();
                    callApi();

                    findViewById(R.id.recent_layout).setVisibility(View.GONE);
                    addSearchKey(searchEdit.getText().toString());

                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.clear_all_txt).setOnClickListener(this);


        setSearchUserAdapter();
    }

    private void setSearchUserAdapter() {
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        recyclerView = findViewById(R.id.recylerview);
        noDataLayout = findViewById(R.id.no_data_layout);
        linearLayoutManager = new LinearLayoutManager(SearchAllUserA.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        dataList = new ArrayList<>();
        usersAdapter = new UsersAdapter(SearchAllUserA.this, dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                UsersModel item = (UsersModel) object;
                Functions.hideSoftKeyboard(SearchAllUserA.this);
                openProfile(item.fb_id, item.username, item.profile_pic);
            }
        });
        recyclerView.setAdapter(usersAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollOutitems = linearLayoutManager.findLastVisibleItemPosition();

                Functions.printLog("resp", "" + scrollOutitems);
                if (userScrolled && (scrollOutitems == dataList.size() - 1)) {
                    userScrolled = false;

                    if (loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinsh) {
                        loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        callApi();
                    }
                }


            }
        });

        loadMoreProgress =findViewById(R.id.load_more_progress);
    }

    public void callApi() {

        JSONObject params = new JSONObject();
        try {

            params.put("type", "user");
            params.put("keyword", searchEdit.getText().toString());
            params.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(SearchAllUserA.this, ApiLinks.search, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(SearchAllUserA.this,resp);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                parseUsers(resp);
            }
        });

    }


    public void parseUsers(String responce) {


        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equalsIgnoreCase("200")) {

                JSONArray msg = jsonObject.optJSONArray("msg");
                ArrayList<UsersModel> temp_list = new ArrayList<>();
                for (int i = 0; i < msg.length(); i++) {
                    JSONObject data = msg.optJSONObject(i);
                    UserModel userDetailModel= DataParsing.getUserDataModel(data.optJSONObject("User"));

                    UsersModel user = new UsersModel();
                    user.fb_id = userDetailModel.getId();
                    user.username = userDetailModel.getUsername();
                    user.first_name = userDetailModel.getFirstName();
                    user.last_name = userDetailModel.getLastName();
                    user.gender = userDetailModel.getGender();

                    user.profile_pic = userDetailModel.getProfilePic();

                    user.followers_count = userDetailModel.getFollowersCount();
                    user.videos =userDetailModel.getVideoCount();

                    temp_list.add(user);


                }

                if (pageCount == 0) {
                    dataList.clear();
                    dataList.addAll(temp_list);

                    if (dataList.isEmpty()) {
                        noDataLayout.setVisibility(View.VISIBLE);
                    } else {
                        noDataLayout.setVisibility(View.GONE);

                        recyclerView.setAdapter(usersAdapter);
                    }
                } else {

                    if (temp_list.isEmpty())
                        ispostFinsh = true;
                    else {
                        dataList.addAll(temp_list);
                        usersAdapter.notifyDataSetChanged();
                    }

                }

            } else {
                if (pageCount==0)
                {
                    dataList.clear();
                }
                if (dataList.isEmpty())
                    noDataLayout.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }

    }

    public void openProfile(String fb_id, String username, String profile_pic) {
        Intent intent=new Intent(SearchAllUserA.this, ProfileA.class);
        intent.putExtra("user_id", fb_id);
        intent.putExtra("user_name", username);
        intent.putExtra("user_pic", profile_pic);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }


    public void addSearchKey(String search_key) {
        ArrayList<String> search_list = (ArrayList<String>) Paper.book().read("recent_search", new ArrayList<String>());
        search_list.add(search_key);
        Paper.book().write("recent_search", search_list);

    }


    RecyclerView recylerviewSuggestion;
    RecentSearchAdapter recentsearchAdapter;
    ArrayList<String> searchQueryList = new ArrayList<>();
    public void showRecentSearch() {
        ArrayList<String> search_list = (ArrayList<String>) Paper.book().read("recent_search", new ArrayList<String>());

        searchQueryList.clear();
        searchQueryList.addAll(search_list);

        if (searchQueryList.isEmpty()) {
            findViewById(R.id.recent_layout).setVisibility(View.GONE);
            return;
        } else {
            findViewById(R.id.recent_layout).setVisibility(View.VISIBLE);
        }

        if (recentsearchAdapter != null) {
            recentsearchAdapter.getFilter().filter(searchEdit.getText().toString());
            recentsearchAdapter.notifyDataSetChanged();
            return;
        }

        findViewById(R.id.recent_layout).setVisibility(View.VISIBLE);
        recentsearchAdapter = new RecentSearchAdapter(SearchAllUserA.this, searchQueryList, new AdapterClickListener() {
            @Override
            public void onItemClick(View v, int pos, Object object) {

                if (v.getId() == R.id.delete_btn) {
                    searchQueryList.remove(object);
                    recentsearchAdapter.notifyDataSetChanged();

                    search_list.remove(object);
                    Paper.book().write("recent_search", search_list);
                } else {

                    String search = (String) object;
                    searchEdit.setText(search);
                    searchEdit.setSelection(search.length());
                    pageCount = 0;
                    shimmerFrameLayout.setVisibility(View.VISIBLE);
                    shimmerFrameLayout.startShimmer();
                    callApi();
                    findViewById(R.id.recent_layout).setVisibility(View.GONE);
                }

            }
        });
        recylerviewSuggestion = findViewById(R.id.recylerview_suggestion);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(SearchAllUserA.this);
        recylerviewSuggestion.setLayoutManager(layoutManager);
        recylerviewSuggestion.setHasFixedSize(true);
        recylerviewSuggestion.setAdapter(recentsearchAdapter);

    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onClick(View view) {
       switch (view.getId())
       {
           case R.id.search_btn:
               Functions.hideSoftKeyboard(SearchAllUserA.this);
               pageCount = 0;
               shimmerFrameLayout.setVisibility(View.VISIBLE);
               shimmerFrameLayout.startShimmer();
               callApi();
               findViewById(R.id.recent_layout).setVisibility(View.GONE);
               addSearchKey(searchEdit.getText().toString());
               break;

           case R.id.clear_all_txt:
               Paper.book().delete("recent_search");
               showRecentSearch();
               break;
           case R.id.ivBack:
               SearchAllUserA.super.onBackPressed();
               break;
       }
    }
}