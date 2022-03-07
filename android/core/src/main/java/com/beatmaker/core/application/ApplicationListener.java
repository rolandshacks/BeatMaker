package com.beatmaker.core.application;

public interface ApplicationListener {
    void onApplicationStartup();
    void onApplicationShutdown();
    void onApplicationPause();
    void onApplicationResume();
}
