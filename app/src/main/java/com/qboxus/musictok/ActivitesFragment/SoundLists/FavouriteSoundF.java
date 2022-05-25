package com.qboxus.musictok.ActivitesFragment.SoundLists;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.qboxus.musictok.Adapters.FavouriteSoundAdapter;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.qboxus.musictok.Models.SoundsModel;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.request.DownloadRequest;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavouriteSoundF extends RootFragment implements Player.Listener {


    Context context;
    View view;
    ArrayList<SoundsModel> datalist;
    FavouriteSoundAdapter adapter;
    static boolean active = false;
    RecyclerView recyclerView;

    DownloadRequest prDownloader;


    public static String runningSoundId;
    ProgressBar pbar;
    SwipeRefreshLayout swiperefresh;
    RelativeLayout noDataLayout;


    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;
    LinearLayoutManager linearLayoutManager;

    public FavouriteSoundF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_sound_list, container, false);

        context = getContext();

        runningSoundId = "none";


        PRDownloader.initialize(context);

        pbar = view.findViewById(R.id.pbar);
        noDataLayout = view.findViewById(R.id.no_data_layout);

        datalist = new ArrayList<>();
        loadMoreProgress = view.findViewById(R.id.load_more_progress);
        recyclerView = view.findViewById(R.id.listview);
        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new FavouriteSoundAdapter(context, datalist, new FavouriteSoundAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, SoundsModel item) {

                if (view.getId() == R.id.done) {
                    stopPlaying();
                    downLoadMp3(item.id, item.sound_name, item.acc_path);
                } else if (view.getId() == R.id.fav_btn) {
                    callApiForFavSound(postion, item.id);
                } else {
                    stopPlaying();
                    playaudio(view, item);
                }

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
                if (userScrolled && (scrollOutitems == datalist.size() - 1)) {
                    userScrolled = false;

                    if (loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinsh) {
                        loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        callApiForGetAllsound();
                    }
                }


            }
        });


        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageCount=0;
                callApiForGetAllsound();
            }
        });


        return view;
    }




    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if ((view != null && visible)) {
                    callApiForGetAllsound();
                } else {
                    stopPlaying();
                }
            }
        },200);
    }


    private void callApiForGetAllsound() {

        if (datalist == null)
            datalist = new ArrayList<>();

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, "0"));
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showFavouriteSounds, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                swiperefresh.setRefreshing(false);
                pbar.setVisibility(View.GONE);
                parseData(resp);
            }
        });


    }


    public void parseData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                JSONArray msgArray = jsonObject.getJSONArray("msg");
                ArrayList<SoundsModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i).optJSONObject("Sound");

                    SoundsModel item = new SoundsModel();

                    item.id = itemdata.optString("id");


                    String accpath = itemdata.optString("audio");
                    if (accpath != null && accpath.contains("http"))
                        item.acc_path = itemdata.optString("audio");
                    else
                        item.acc_path = Constants.BASE_URL + itemdata.optString("audio");


                    item.sound_name = itemdata.optString("name");
                    item.description = itemdata.optString("description");
                    item.section = itemdata.optString("section");

                    String thum_image = itemdata.optString("thum");

                    if (thum_image != null && thum_image.contains("http"))
                        item.thum = itemdata.optString("thum");
                    else
                        item.thum = Constants.BASE_URL + itemdata.optString("thum");

                    item.duration = itemdata.optString("duration");
                    item.date_created = itemdata.optString("created");

                    temp_list.add(item);


                }

                if (pageCount == 0) {
                    datalist.clear();
                    datalist.addAll(temp_list);
                } else {
                    datalist.addAll(temp_list);
                }

                adapter.notifyDataSetChanged();
            }

            if (datalist.isEmpty()) {
                view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onBackPressed() {
        getActivity().onBackPressed();
        return super.onBackPressed();
    }


    //initialize the player for play the audio file

    View previousView;
    SimpleExoPlayer player;
    String previousUrl = "none";

    public void playaudio(View view, final SoundsModel item) {
        previousView = view;

        if (previousUrl.equals(item.acc_path)) {

            previousUrl = "none";
            runningSoundId = "none";
        } else {

            previousUrl = item.acc_path;
            runningSoundId = item.id;

            DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);

            player = new SimpleExoPlayer.Builder(context).
                    setTrackSelector(trackSelector)
                    .build();


            DataSource.Factory cacheDataSourceFactory = new DefaultDataSourceFactory(view.getContext(), context.getString(R.string.app_name));
            MediaSource videoSource = new ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(MediaItem.fromUri(item.acc_path));
            player.addMediaSource(videoSource);
            player.prepare();
            player.addListener(this);


            player.setPlayWhenReady(true);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setUsage(C.USAGE_MEDIA)
                            .setContentType(C.CONTENT_TYPE_MOVIE)
                            .build();
                    player.setAudioAttributes(audioAttributes, true);
                }
            }
            catch (Exception e)
            {
                Log.d(Constants.tag,"Exception audio focus : "+e);
            }
        }

    }


    // stop the player
    public void stopPlaying() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        showStopState();

    }


    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;

        runningSoundId = "null";

        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        showStopState();

    }


    // show the player states
    public void showRunState() {

        if (previousView != null) {
            previousView.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previousView.findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
            View imgDone=previousView.findViewById(R.id.done);
            View imgFav=previousView.findViewById(R.id.fav_btn);
            imgFav.animate().translationX(0).setDuration(400).start();
            imgDone.animate().translationX(0).setDuration(400).start();
        }

    }


    // show the loading states
    public void showLoadingState() {
        previousView.findViewById(R.id.play_btn).setVisibility(View.GONE);
        previousView.findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
    }


    public void showStopState() {

        if (previousView != null) {
            previousView.findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
            previousView.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previousView.findViewById(R.id.pause_btn).setVisibility(View.GONE);
            View imgDone=previousView.findViewById(R.id.done);
            View imgFav=previousView.findViewById(R.id.fav_btn);
            imgDone.animate().translationX(Float.valueOf(""+getResources().getDimension(R.dimen._80sdp))).setDuration(400).start();
            imgFav.animate().translationX(Float.valueOf(""+getResources().getDimension(R.dimen._50sdp))).setDuration(400).start();
        }

        runningSoundId = "none";

    }


    // download the audio file
    public void downLoadMp3(final String id, final String sound_name, String url) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(view.getContext().getString(R.string.please_wait_));
        progressDialog.show();

        prDownloader = PRDownloader.download(url, Functions.getAppFolder(getActivity())+Variables.APP_HIDED_FOLDER, Variables.SelectedAudio_AAC)
                .build();

        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                progressDialog.dismiss();
                Intent output = new Intent();
                output.putExtra("isSelected", "yes");
                output.putExtra("sound_name", sound_name);
                output.putExtra("sound_id", id);
                getActivity().setResult(RESULT_OK, output);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);
            }

            @Override
            public void onError(Error error) {
                progressDialog.dismiss();
            }
        });

    }


    private void callApiForFavSound(final int pos, String sound_id) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, "0"));
            parameters.put("sound_id", sound_id);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(context, false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.addSoundFavourite, parameters, Functions.getHeaders(getActivity()),new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();
                datalist.remove(pos);
                adapter.notifyItemRemoved(pos);
                adapter.notifyDataSetChanged();

                if (!datalist.isEmpty())
                    view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);
                else
                    view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);

            }
        });


    }


    // handler will be call on player state change
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playbackState == Player.STATE_BUFFERING) {
            showLoadingState();
        } else if (playbackState == Player.STATE_READY) {
            showRunState();
        } else if (playbackState == Player.STATE_ENDED) {
            showStopState();
        }

    }

}
