package com.beatmaker.app;

import com.beatmaker.core.application.ApplicationBase;
import com.beatmaker.io.JavaMidi;

public class DesktopApplication extends ApplicationBase {

    public DesktopApplication() {
        super();
    }

    @Override
    public void create() {
        midi = new JavaMidi();
        super.create();
    }

}
