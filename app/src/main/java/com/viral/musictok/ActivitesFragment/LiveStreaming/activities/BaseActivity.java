package com.viral.musictok.ActivitesFragment.LiveStreaming.activities;

import android.os.Bundle;

import com.viral.musictok.SimpleClasses.AppCompatLocaleActivity;

import com.viral.musictok.ActivitesFragment.LiveStreaming.rtc.EngineConfig;
import com.viral.musictok.ActivitesFragment.LiveStreaming.rtc.EventHandler;
import com.viral.musictok.ActivitesFragment.LiveStreaming.stats.StatsManager;
import com.viral.musictok.SimpleClasses.TicTic;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public abstract class BaseActivity extends AppCompatLocaleActivity implements EventHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected TicTic application() {
        return (TicTic) getApplication();
    }

    protected RtcEngine rtcEngine() {
        return application().rtcEngine();
    }

    protected EngineConfig config() {
        return application().engineConfig();
    }

    protected StatsManager statsManager() {
        return application().statsManager();
    }

    protected void registerRtcEventHandler(EventHandler handler) {
        application().registerEventHandler(handler);
    }

    protected void removeRtcEventHandler(EventHandler handler) {
        application().removeEventHandler(handler);
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {

    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {

    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {

    }

    @Override
    public void onUserOffline(int uid, int reason) {

    }

    @Override
    public void onUserJoined(int uid, int elapsed) {

    }

    @Override
    public void onLastmileQuality(final int quality) {

    }

    @Override
    public void onLastmileProbeResult(final IRtcEngineEventHandler.LastmileProbeResult result) {

    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {

    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {

    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {

    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {

    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {

    }
}
