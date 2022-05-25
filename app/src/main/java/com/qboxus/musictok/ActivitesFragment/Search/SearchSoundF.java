package com.qboxus.musictok.ActivitesFragment.Search;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.qboxus.musictok.Adapters.SoundListAdapter;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
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
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchSoundF extends RootFragment implements Player.Listener {

    View view;
    Context context;
    String type;
    ShimmerFrameLayout shimmerFrameLayout;
    ArrayList<Object> dataList;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;
    SoundListAdapter adapter;
    static boolean active = false;
    RelativeLayout noDataLayout;
    DownloadRequest prDownloader;

    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;


    public static String runningSoundId;

    public SearchSoundF(String type) {
        this.type = type;
    }

    public SearchSoundF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sound_list, container, false);
        context = view.getContext();

        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        recyclerView = view.findViewById(R.id.recylerview);
        noDataLayout = view.findViewById(R.id.no_data_layout);
        linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        dataList = new ArrayList<>();
        adapter = new SoundListAdapter(context, dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

                SoundsModel item = (SoundsModel) object;

                if (view.getId() == R.id.done) {
                    stopPlaying();
                    downLoadMp3(item.id, item.sound_name, item.acc_path);
                } else if (view.getId() == R.id.fav_btn) {
                    callApiForFavSound(pos, item);
                } else {
                    if (thread != null && !thread.isAlive()) {
                        stopPlaying();
                        playaudio(view, item);
                    } else if (thread == null) {
                        stopPlaying();
                        playaudio(view, item);
                    }
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


        loadMoreProgress = view.findViewById(R.id.load_more_progress);
        pageCount = 0;

        return view;
    }


    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if ((view != null && menuVisible) && dataList.isEmpty()) {
            callApi();
        }
    }


    // get the list of sounds against typed keyword
    public void callApi() {

        JSONObject params = new JSONObject();
        try {
            if (Functions.getSharedPreference(context).getString(Variables.U_ID, null) != null) {
                params.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, "0"));
            }

            params.put("type", type);
            params.put("keyword", SearchMainA.searchEdit.getText().toString());
            params.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.search, params,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                if (type.equals("sound"))
                    parseSounds(resp);
            }
        });

    }


    // parse the sound list date into model
    private void parseSounds(String responce) {


        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                JSONArray msgArray = jsonObject.getJSONArray("msg");
                ArrayList<Object> temp_list = new ArrayList<>();
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    JSONObject sound = itemdata.optJSONObject("Sound");

                    SoundsModel item = new SoundsModel();

                    item.id = sound.optString("id");

                    item.acc_path = sound.optString("audio");


                    item.sound_name = sound.optString("name");
                    item.description = sound.optString("description");
                    item.section = sound.optString("section");

                    String thum_image = sound.optString("thum", "");

                    if (thum_image != null && thum_image.contains("http"))
                        item.thum = sound.optString("thum");
                    else
                        item.thum = Constants.BASE_URL + itemdata.optString("thum");

                    item.duration = sound.optString("duration");
                    item.date_created = sound.optString("created");
                    item.fav = sound.optString("favourite");

                    temp_list.add(item);
                }


                if (pageCount == 0) {

                    dataList.addAll(temp_list);

                    if (dataList.isEmpty()) {
                        noDataLayout.setVisibility(View.VISIBLE);
                    } else {
                        noDataLayout.setVisibility(View.GONE);

                        recyclerView.setAdapter(adapter);
                    }
                } else {

                    if (temp_list.isEmpty())
                        ispostFinsh = true;
                    else {
                        dataList.addAll(temp_list);
                        adapter.notifyDataSetChanged();
                    }

                }


            } else {
                if (dataList.isEmpty())
                    noDataLayout.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }
    }


    private void callApiForFavSound(final int pos, final SoundsModel item) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, "0"));
            parameters.put("sound_id", item.id);


        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(context, false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.addSoundFavourite, parameters, Functions.getHeaders(getActivity()),new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();

                if (item.fav.equals("1"))
                    item.fav = "0";
                else
                    item.fav = "1";

                dataList.remove(item);
                dataList.add(pos, item);
                adapter.notifyDataSetChanged();

                adapter.notifyDataSetChanged();

            }
        });

    }


    View previousView;
    Thread thread;
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


    public void downLoadMp3(final String id, final String sound_name, String url) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        prDownloader = PRDownloader.download(url, Functions.getAppFolder(getActivity())+Variables.APP_HIDED_FOLDER, sound_name + id)
                .build();

        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                progressDialog.dismiss();
                Functions.showToast(context, view.getContext().getString(R.string.audio_saved_in_your_phone));
            }

            @Override
            public void onError(Error error) {
                progressDialog.dismiss();
            }
        });

    }


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
