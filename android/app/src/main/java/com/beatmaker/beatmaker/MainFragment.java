package com.beatmaker.beatmaker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.beatmaker.beatmaker.databinding.FragmentMainBinding;
import com.beatmaker.core.application.ApplicationBase;
import com.beatmaker.core.application.ApplicationListener;
import com.beatmaker.core.midi.MidiNote;
import com.beatmaker.core.sequencer.Sequencer;
import com.beatmaker.core.sequencer.SequencerControl;
import com.beatmaker.core.sequencer.SequencerListener;
import com.beatmaker.core.sequencer.SequencerMetrics;
import com.beatmaker.core.sequencer.SequencerPosition;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements ApplicationListener, SequencerListener {

    private FragmentMainBinding ui;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ui = FragmentMainBinding.inflate(inflater, container, false);
        View view = ui.getRoot();

        ui.btnRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Sequencer sequencer = Sequencer.instance();
                if (null != sequencer) {
                    sequencer.rewind();
                }

            }
        });

        ui.btnStartStop.setOnClickListener(v -> {
            Sequencer sequencer = Sequencer.instance();
            if (null == sequencer) return;

            int mode = sequencer.getMode();
            if (mode == SequencerControl.MODE_PLAYING) {
                sequencer.stop();
            } else if (mode == SequencerControl.MODE_PAUSED) {
                sequencer.resume();
            } else if (mode == SequencerControl.MODE_STOPPED) {
                sequencer.start();
            }
        });

        ApplicationBase.instance().addListener(this);
        Sequencer.instance().addListener(this);

        return view;
    }

    public void onSequencerPositionUpdate(SequencerPosition position, boolean stepChange) {
        if (!stepChange) return;

        if (null == ui) return;

        if (null != ui.mainView) {
            ui.mainView.setDirty();
        }

        if (null != ui.textPosition) {
            String posInfo = "" + (position.getQuarterInMeasure()+1) + "." + (position.getStepInQuarter()+1);
            ui.textPosition.setText(posInfo);
        }

        if (null != ui.textBpm) {
            String bpmInfo = SequencerMetrics.instance().getBpm() + " bpm";
            ui.textBpm.setText(bpmInfo);
        }
    }

    public void onSequencerCaptureFinished(List<MidiNote> capturedNotes) {
    }

    @Override
    public void onApplicationStartup() {
        //ui.mainView.start();
    }

    @Override
    public void onApplicationShutdown() {
        ui.mainView.stop();
    }

    @Override
    public void onApplicationPause() {
        ui.mainView.stop();
    }

    @Override
    public void onApplicationResume() {
        ui.mainView.start();
    }


}
