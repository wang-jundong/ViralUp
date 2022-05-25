package com.qboxus.musictok.ActivitesFragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.hendraanggrian.appcompat.widget.SocialView;
import com.qboxus.musictok.ActivitesFragment.Profile.ProfileA;
import com.qboxus.musictok.Adapters.CommentsAdapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.APICallBack;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentDataSend;
import com.qboxus.musictok.MainMenu.MainMenuFragment;
import com.qboxus.musictok.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.qboxus.musictok.Models.CommentModel;
import com.qboxus.musictok.Models.HomeModel;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.SoftKeyboardStateHelper;
import com.qboxus.musictok.SimpleClasses.Variables;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentF extends RootFragment {

    View view;
    Context context;

    RecyclerView recyclerView;

    CommentsAdapter adapter;

    ArrayList<CommentModel> dataList;
    HomeModel item;
    String videoId;
    String userId;

    EditText messageEdit;
    ImageView sendBtn;
    ProgressBar sendProgress,noDataLoader;

    TextView commentCountTxt, noDataLayout;

    FrameLayout commentScreen;
    String commentId, parentCommentId, replyUserName;
    String replyStatus = null;
    public static int commentCount = 0;



    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;
    LinearLayoutManager linearLayoutManager;


    public CommentF() {

    }

    FragmentDataSend fragmentDataSend;

    RelativeLayout write_layout;


    @SuppressLint("ValidFragment")
    public CommentF(int count, FragmentDataSend fragmentDataSend ) {
        commentCount = count;
        this.fragmentDataSend = fragmentDataSend;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_comment, container, false);
        context = getContext();
        write_layout = view.findViewById(R.id.write_layout);
        commentScreen = view.findViewById(R.id.comment_screen);
        noDataLayout = view.findViewById(R.id.no_data_layout);
        commentScreen.setOnClickListener(v -> {

            getActivity().onBackPressed();

        });

        view.findViewById(R.id.goBack).setOnClickListener(v -> {

            getActivity().onBackPressed();

        });


        Bundle bundle = getArguments();
        if (bundle != null) {
            videoId = bundle.getString("video_id");
            userId = bundle.getString("user_id");
            item = (HomeModel) bundle.getSerializable("data");
        }


        SoftKeyboardStateHelper.SoftKeyboardStateListener softKeyboardStateListener = new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {

            }

            @Override
            public void onSoftKeyboardClosed() {
                if ((parentCommentId != null && !parentCommentId.equals("")) || replyStatus != null) {
                    messageEdit.setHint(context.getString(R.string.leave_a_comment));
                    replyStatus = null;
                    parentCommentId = null;
                }
            }
        };


        final SoftKeyboardStateHelper softKeyboardStateHelper = new SoftKeyboardStateHelper(context, view.findViewById(R.id.comment_screen));
        softKeyboardStateHelper.addSoftKeyboardStateListener(softKeyboardStateListener);


        commentCountTxt = view.findViewById(R.id.comment_count);


        loadMoreProgress = view.findViewById(R.id.load_more_progress);
        recyclerView = view.findViewById(R.id.recylerview);
        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(false);

        dataList = new ArrayList<>();
        adapter = new CommentsAdapter(context, dataList, new CommentsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int positon, CommentModel item, View view) {

                switch (view.getId()) {

                    case R.id.user_pic:
                        openProfile();
                        break;

                    case R.id.message_layout:
                        messageEdit.setHint(context.getString(R.string.reply_to)+" " + item.user_name);
                        messageEdit.requestFocus();
                        replyStatus = "reply";
                        commentId = item.comment_id;
                        Functions.showKeyboard(getActivity());
                        break;


                    case R.id.like_layout:
                        if (Functions.checkLoginUser(getActivity())) {
                            likeComment(positon, item);
                        }
                        break;


                }
            }
        }, new CommentsAdapter.onRelyItemCLickListener() {
            @Override
            public void onItemClick(ArrayList<CommentModel> arrayList, int postion, View view) {
                switch (view.getId()) {

                    case R.id.user_pic:
                        CommentModel item = arrayList.get(postion);
                        openProfile();
                        break;


                    case R.id.reply_layout:
                        replyUserName = arrayList.get(postion).replay_user_name;
                        messageEdit.setHint(context.getString(R.string.reply_to)+" " + replyUserName);
                        messageEdit.requestFocus();
                        replyStatus = "reply";
                        commentId = arrayList.get(postion).comment_reply_id;
                        parentCommentId = arrayList.get(postion).parent_comment_id;
                        Functions.showKeyboard(getActivity());
                        break;


                    case R.id.like_layout:
                        if (Functions.checkLoginUser(getActivity())) {
                            likeCommentReply(postion, arrayList.get(postion));
                        }
                        break;
                }
            }
        }, new CommentsAdapter.LinkClickListener() {
            @Override
            public void onLinkClicked(SocialView view, String matchedText) {
                Functions.hideSoftKeyboard(getActivity());
                openProfileByUsername(matchedText);
            }
        });

        recyclerView.setAdapter(adapter);
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
                        getAllComments();
                    }
                }


            }
        });


        messageEdit = view.findViewById(R.id.message_edit);

        noDataLoader=view.findViewById(R.id.noDataLoader);
        sendProgress = view.findViewById(R.id.send_progress);
        sendBtn = view.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(v -> {
            String message = messageEdit.getText().toString();
            if (!TextUtils.isEmpty(message)) {

                if (Functions.checkLoginUser(getActivity())) {

                    if (replyStatus == null) {
                        sendComments(videoId, message);
                    } else if (parentCommentId != null && !parentCommentId.equals("")) {
                        message = context.getString(R.string.replied_to)+" " + "@" + replyUserName + " " + message;
                        sendCommentsReply(parentCommentId, message);
                    } else {
                        sendCommentsReply(commentId, message);
                    }
                    messageEdit.setText(null);
                    sendProgress.setVisibility(View.VISIBLE);
                    sendBtn.setVisibility(View.GONE);

                }

            }

        });


        if (Functions.isShowContentPrivacy(context, item.apply_privacy_model.getVideo_comment(), item.follow_status_button.equalsIgnoreCase("friends"))) {
            sendBtn.setEnabled(true);
        } else
            sendBtn.setEnabled(false);


        if (item.apply_privacy_model.getVideo_comment().equalsIgnoreCase("everyone") ||
                (item.apply_privacy_model.getVideo_comment().equalsIgnoreCase("friend") &&
                        item.follow_status_button.equalsIgnoreCase("friends")))
        {
            write_layout.setVisibility(View.VISIBLE);
            getAllComments();
        }
        else
        {
            noDataLoader.setVisibility(View.GONE);
            write_layout.setVisibility(View.GONE);
            noDataLayout.setText(view.getContext().getString(R.string.comments_are_turned_off));
            commentCountTxt.setText("0 "+context.getString(R.string.comments));
            noDataLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        }

        return view;
    }




    private void likeCommentReply(int postion, CommentModel item) {

        String action = item.comment_reply_liked;

        if (action != null) {
            if (action.equals("1")) {
                action = "0";
                item.reply_liked_count = "" + (Functions.parseInterger(item.reply_liked_count) - 1);
            } else {
                action = "1";
                item.reply_liked_count = "" + (Functions.parseInterger(item.reply_liked_count) + 1);
            }
        }


        for (int i = 0; i < dataList.size(); i++) {

            if (!dataList.isEmpty() && !dataList.get(i).arrayList.isEmpty()) {

                if (item.parent_comment_id.equals(dataList.get(i).comment_id)) {

                    dataList.get(i).arrayList.remove(postion);
                    item.comment_reply_liked = action;
                    dataList.get(i).arrayList.add(postion, item);

                }

            }
        }
        adapter.notifyDataSetChanged();
        Functions.callApiForLikeCommentReply(

                getActivity(), item.comment_reply_id, new

                        APICallBack() {
                            @Override
                            public void arrayData(ArrayList arrayList) {

                            }

                            @Override
                            public void onSuccess(String responce) {

                            }

                            @Override
                            public void onFail(String responce) {

                            }
                        });
    }


    private void likeComment(int positon, CommentModel item) {

        String action = item.liked;

        if (action != null) {
            if (action.equals("1")) {
                action = "0";
                item.like_count = "" + (Functions.parseInterger(item.like_count) - 1);
            } else {
                action = "1";
                item.like_count = "" + (Functions.parseInterger(item.like_count) + 1);
            }


            dataList.remove(positon);
            item.liked = action;
            dataList.add(positon, item);
            adapter.notifyDataSetChanged();
        }
        Functions.callApiForLikeComment(getActivity(), item.comment_id, new APICallBack() {
            @Override
            public void arrayData(ArrayList arrayList) {

            }

            @Override
            public void onSuccess(String responce) {

            }

            @Override
            public void onFail(String responce) {

            }
        });
    }


    @Override
    public void onDetach() {
        Functions.hideSoftKeyboard(getActivity());
        super.onDetach();
    }

    // this funtion will get all the comments against post
    public void getAllComments() {
        if (dataList == null)
        {
            dataList = new ArrayList<>();
            noDataLoader.setVisibility(View.VISIBLE);
        }


        JSONObject parameters = new JSONObject();
        try {
            parameters.put("video_id", videoId);
            if (Functions.getSharedPreference(view.getContext()).getBoolean(Variables.IS_LOGIN, false)) {
                parameters.put("user_id", Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID, "0"));
            }
            parameters.put("starting_point", "" + pageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showVideoComments, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                noDataLoader.setVisibility(View.GONE);

                ArrayList<CommentModel> temp_list = new ArrayList<>();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    if (code.equals("200")) {

                        JSONArray msgArray = response.getJSONArray("msg");

                        for (int i = 0; i < msgArray.length(); i++) {
                            JSONObject itemdata = msgArray.optJSONObject(i);

                            JSONObject videoComment = itemdata.optJSONObject("VideoComment");
                            UserModel userDetailModel= DataParsing.getUserDataModel(itemdata.optJSONObject("User"));

                            JSONArray videoCommentReply = itemdata.optJSONArray("VideoCommentReply");

                            ArrayList<CommentModel> replyList = new ArrayList<>();
                            if (videoCommentReply.length() > 0) {
                                for (int j = 0; j < videoCommentReply.length(); j++) {
                                    JSONObject jsonObject = videoCommentReply.getJSONObject(j);

                                    UserModel userDetailModelReply=DataParsing.getUserDataModel(jsonObject.optJSONObject("User"));
                                    CommentModel comment_model = new CommentModel();

                                    comment_model.comment_reply_id = jsonObject.optString("id");
                                    comment_model.reply_liked_count = jsonObject.optString("like_count");
                                    comment_model.comment_reply_liked = jsonObject.optString("like");
                                    comment_model.comment_reply = jsonObject.optString("comment");
                                    comment_model.created = jsonObject.optString("created");


                                    comment_model.replay_user_name = userDetailModelReply.getUsername();
                                    comment_model.replay_user_url = userDetailModelReply.getProfilePic();
                                    comment_model.parent_comment_id = videoComment.optString("id");

                                    replyList.add(comment_model);
                                }
                            }

                            CommentModel item = new CommentModel();


                            item.fb_id = userDetailModel.getId();
                            item.user_name = userDetailModel.getUsername();
                            item.first_name = userDetailModel.getFirstName();
                            item.last_name = userDetailModel.getLastName();
                            item.arraylist_size = String.valueOf(videoCommentReply.length());
                            item.profile_pic = userDetailModel.getProfilePic();

                            item.arrayList = replyList;
                            item.video_id = videoComment.optString("video_id");
                            item.comments = videoComment.optString("comment");
                            item.liked = videoComment.optString("like");
                            item.like_count = videoComment.optString("like_count");
                            item.comment_id = videoComment.optString("id");
                            item.created = videoComment.optString("created");


                            temp_list.add(item);

                        }

                        if (pageCount == 0) {
                            dataList.clear();
                            dataList.addAll(temp_list);
                        } else {
                            dataList.addAll(temp_list);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    if (dataList.isEmpty()) {
                        noDataLayout.setVisibility(View.VISIBLE);
                    } else {
                        noDataLayout.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    loadMoreProgress.setVisibility(View.GONE);
                }
            }
        });
    }



    // this function will call an api to upload your comment reply
    private void sendCommentsReply(String commentId, String message) {
        Functions.callApiForSendCommentReply(getActivity(), commentId, message, new APICallBack() {
            @Override
            public void arrayData(ArrayList arrayList) {
                sendProgress.setVisibility(View.GONE);
                sendBtn.setVisibility(View.VISIBLE);
                Functions.hideSoftKeyboard(getActivity());
                messageEdit.setHint(context.getString(R.string.leave_a_comment));

                ArrayList<CommentModel> arrayList1 = arrayList;

                for (int i = 0; i < dataList.size(); i++) {
                    if (parentCommentId != null) {
                        if (parentCommentId.equals(dataList.get(i).comment_id)) {
                            for (CommentModel item : arrayList1) {
                                dataList.get(i).arrayList.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        if (commentId.equals(dataList.get(i).comment_id)) {
                            CommentModel comment_model = new CommentModel();
                            comment_model.item_count_replies = "1";
                            for (CommentModel item : arrayList1) {
                                dataList.get(i).arrayList.add(item);
                            }
                        }


                        adapter.notifyDataSetChanged();
                    }

                }
                replyStatus = null;
                parentCommentId = null;
            }

            @Override
            public void onSuccess(String responce) {
                // this will return a string responce
            }

            @Override
            public void onFail(String responce) {

                // this will return the failed responce
            }

        });

    }

    // this function will call an api to upload your comment
    public void sendComments(String video_id, final String comment) {

        Functions.callApiForSendComment(getActivity(), video_id, comment, new APICallBack() {
            @Override
            public void arrayData(ArrayList arrayList) {
                sendProgress.setVisibility(View.GONE);
                sendBtn.setVisibility(View.VISIBLE);
                noDataLayout.setVisibility(View.GONE);
                Functions.hideSoftKeyboard(getActivity());
                ArrayList<CommentModel> arrayList1 = arrayList;
                for (CommentModel item : arrayList1) {
                    dataList.add(0, item);
                    commentCount++;
                    commentCountTxt.setText(commentCount + " "+context.getString(R.string.comments));

                    if (fragmentDataSend != null)
                        fragmentDataSend.onDataSent("" + commentCount);

                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onSuccess(String responce) {
                // this will return a string responce
            }

            @Override
            public void onFail(String responce) {

                // this will return the failed responce
            }

        });

    }


      // get the profile data by sending the username instead of id
    private void openProfileByUsername(String username) {

        if (Functions.getSharedPreference(context).getString(Variables.U_NAME, "0").equals(username)) {

            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        } else {

            Intent intent=new Intent(view.getContext(), ProfileA.class);
            intent.putExtra("user_name", username);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

        }


    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void openProfile() {

        if (!Functions.getSharedPreference(context).getString(Variables.U_ID, "0").equals(item.user_id)) {


            Intent intent=new Intent(view.getContext(), ProfileA.class);
            intent.putExtra("user_id", item.user_id);
            intent.putExtra("user_name", item.username);
            intent.putExtra("user_pic", item.profile_pic);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);

        }


    }








}
