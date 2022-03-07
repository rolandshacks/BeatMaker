package com.beatmaker.core.utils;

public interface SystemCalls {
    void forceTerminateApplication();
    void setPriority(int priority);
    int getPriority();
}
