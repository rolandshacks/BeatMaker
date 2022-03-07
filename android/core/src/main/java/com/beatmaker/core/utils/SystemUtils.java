package com.beatmaker.core.utils;

public class SystemUtils {

    private static SystemCalls sys = null;

    public static void setSystemCallInterface(SystemCalls sys) {
        SystemUtils.sys = sys;
    }

    public static void forceTerminateApplication() {
        if (null == sys) return;
        sys.forceTerminateApplication();
    }

    public static void setPriority(int priority) {
        if (null == sys) return;
        sys.setPriority(priority);
    }

    public static int getPriority() {
        if (null == sys) return 0;
        return sys.getPriority();
    }

}
