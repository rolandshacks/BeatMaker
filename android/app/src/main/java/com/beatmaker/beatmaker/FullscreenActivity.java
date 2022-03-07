package com.beatmaker.beatmaker;

import android.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.beatmaker.app.Application;
import com.beatmaker.beatmaker.databinding.ActivityFullscreenBinding;
import com.beatmaker.core.application.ApplicationBase;
import com.beatmaker.core.sequencer.SequencerElement;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    private static final String TAG = "FullscreenActivity";
    private Resources resources;
    private MainFragment mainPanel;
    private SidePanelFragment sidePanel;
    private boolean sidePanelVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Application application = new Application(getApplicationContext());
        application.create();

        setFullscreen();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if (null == Resources.instance()) {
            new Resources(this);
        }

        ActivityFullscreenBinding ui = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(ui.getRoot());

        sidePanel = (SidePanelFragment) getSupportFragmentManager().findFragmentById(R.id.sidePanelFragment);

        if (!Application.OPEN_SETTINGS_AT_START) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(sidePanel);
            ft.commit();
        }

        mainPanel = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment);

        /*
        mainPanel.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSidePanel();
            }
        });
        */

        BeatView beatView = (BeatView) findViewById(R.id.mainView);
        if (null != beatView) {

            beatView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //hideSidePanel();
                }
            });

            beatView.setActionListener(new BeatView.ActionListener() {
                @Override
                public void onFocus() {
                    //hideSidePanel();
                }

                @Override
                public void onSelect(SequencerElement element) {
                    if (null != element) {
                        showSidePanel(element);
                    } else {
                        hideSidePanel();
                    }
                }
            });
        }

        application.addListener(mainPanel);
        application.startup();

    }

    @Override
    protected void onDestroy() {
        ApplicationBase.instance().destroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullscreen();
        ApplicationBase.instance().resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationBase.instance().pause();
    }

    private void setFullscreen() {

        Window window = getWindow();
        View decorView = window.getDecorView();

        if (null != decorView) {
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        ActionBar actionBar = getActionBar();
        if (null != actionBar) actionBar.hide();
    }

    private void toggleFragment(Fragment fragment, boolean show) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (show) {
            if (fragment.isAdded() && fragment.isHidden()) {
                ft.show(fragment);
                ft.addToBackStack("Settings Panel");
            }
        } else {
            if (fragment.isAdded() && fragment.isVisible()) {
                ft.hide(fragment);
            }
        }

        ft.commit();
    }

    private void hideSidePanel() {
        if (!sidePanel.isVisible()) return;
        sidePanel.onChangeVisibility(false, null);
        toggleFragment(sidePanel, false);
    }

    private void showSidePanel(SequencerElement element) {
        if (sidePanel.isVisible()) {
            sidePanel.onChangeVisibility(true, element);
        } else {
            sidePanel.onChangeVisibility(true, element);
            toggleFragment(sidePanel, true);
        }
    }
}
