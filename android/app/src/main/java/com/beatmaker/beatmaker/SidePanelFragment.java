package com.beatmaker.beatmaker;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.beatmaker.beatmaker.databinding.FragmentSidePanelBinding;
import com.beatmaker.core.midi.MidiNote;
import com.beatmaker.core.sequencer.ElementConfig;
import com.beatmaker.core.sequencer.Sequencer;
import com.beatmaker.core.sequencer.SequencerElement;
import com.beatmaker.core.sequencer.SequencerListener;
import com.beatmaker.core.sequencer.SequencerPosition;
import com.beatmaker.core.sequencer.SequencerStep;
import com.beatmaker.core.sequencer.SequencerTrack;
import com.beatmaker.ui.BitmapButton;
import com.beatmaker.ui.NumberInput;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SidePanelFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SidePanelFragment extends Fragment implements SequencerListener {

    private static final String TAG = "SidePanelFragment";

    private FragmentSidePanelBinding ui;
    private SequencerTrack currentTrack;
    private SequencerStep currentStep;

    public SidePanelFragment() {}

    public static SidePanelFragment newInstance(String param1, String param2) {
        SidePanelFragment fragment = new SidePanelFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ui = FragmentSidePanelBinding.inflate(inflater, container, false);
        View view = ui.getRoot();

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {}
        });

        ui.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTrack = null;
                currentStep = null;
                hideSelf();
            }
        });

        ui.btnReset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                resetConfig();
            }
        });

        ui.btnCapture.setOnCheckedChangeListener(new BitmapButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(BitmapButton button, boolean isChecked) {
                if (isChecked) {
                    if (null != currentTrack) {
                        Sequencer sequencer = Sequencer.instance();
                        if (null != sequencer) {
                            sequencer.startCapture();
                        }
                    } else {
                        ui.btnCapture.setChecked(false);
                    }
                } else {
                    Sequencer sequencer = Sequencer.instance();
                    if (null != sequencer) {
                        sequencer.stopCapture();
                    }
                }
            }
        });

        ui.configMidiChannel.setOnValueChangedListener(new NumberInput.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberInput widget, int oldVal, int newVal) {
                ElementConfig config = getOrCreateConfig();
                if (null != config) {
                    config.setChannel(newVal-1);
                    updateTitle();
                }
            }
        });

        ui.configMidiPitch.setOnValueChangedListener(new NumberInput.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberInput widget, int oldVal, int newVal) {
                ElementConfig config = getOrCreateConfig();
                if (null != config && config.hasSingleNote()) {
                    MidiNote note = config.getSingleNote();
                    note.setPitch(ui.configMidiPitch.getValue());
                    updateTitle();
                }
            }
        });

        ui.configMidiPitch.setValueNameProvider(new NumberInput.ValueNameProvider() {
            @Override
            public String provideName(NumberInput widget, int midiPitch) {
                return MidiNote.getNoteName(midiPitch);
            }
        });

        ui.configMidiVelocity.setOnValueChangedListener(new NumberInput.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberInput widget, int oldVal, int newVal) {
                ElementConfig config = getOrCreateConfig();
                if (null != config && config.hasSingleNote()) {
                    MidiNote note = config.getSingleNote();
                    note.setVelocity(ui.configMidiVelocity.getValue());
                    updateTitle();
                }
            }
        });

        loadConfig();

        Sequencer.instance().addListener(this);

        return view;
    }

    private ElementConfig getConfig() {
        if (null != currentStep && currentStep.hasConfig()) {
            return currentStep.getConfig();
        } else if (null != currentTrack) {
            return currentTrack.getConfig();
        }
        return null;
    }

    private ElementConfig getOrCreateConfig() {
        if (null != currentStep) {

            if (currentStep.hasConfig()) {
                return currentStep.getConfig();
            } else {
                currentStep.setConfig(new ElementConfig(currentTrack.getConfig()));
            }

        } else if (null != currentTrack) {
            return currentTrack.getConfig();
        }

        return null;
    }

    private void resetConfig() {

        if (null == currentTrack) {
            return;
        }

        if (null != currentStep) {
            currentStep.clearConfig();
        } else if (null != currentTrack) {
            currentTrack.getConfig().reset();
        }

        loadConfig();

    }

    public void onChangeVisibility(boolean show, SequencerElement element) {
        if (!show) {
            Sequencer sequencer = Sequencer.instance();
            if (null != sequencer) {
                sequencer.stopCapture();
            }
            ui.btnCapture.setChecked(false);

            currentTrack = null;
            currentStep = null;
            return;
        }

        if (element.isStep()) {
            currentStep = (SequencerStep) element;
            currentTrack = (SequencerTrack) element.getParent();
        } else {
            currentStep = null;
            currentTrack = (SequencerTrack) element;
        }

        loadConfig();
    }

    private void hideSelf() {
        if (!isAdded() || !isVisible()) return;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(this);
        ft.commit();
    }

    private void updateTitle() {

        ElementConfig config = getConfig();

        if (null != currentStep) {
            String text = "Element " + (currentTrack.getIndex()+1) + "/" + (currentStep.getIndex()+1);
            if (currentStep.hasConfig()) {
                text += "*";
            }
            ui.textTitle.setText(text);
        } else if (null != currentTrack) {
            ui.textTitle.setText("Track " + (currentTrack.getIndex()+1));
        } else {
            ui.textTitle.setText("");
        }
    }

    private void loadConfig() {

        ElementConfig config = getConfig();

        if (null == config) {
            ui.textTitle.setText("");
            ui.sectionData.setVisibility(View.GONE);
            ui.sectionNote.setVisibility(View.VISIBLE);
            ui.sectionVelocity.setVisibility(View.VISIBLE);
            return;
        }

        ui.configMidiChannel.setValue(config.getChannel()+1, false);

        updateTitle();

        if (config.hasSingleNote()) {

            ui.sectionData.setVisibility(View.GONE);
            ui.sectionNote.setVisibility(View.VISIBLE);
            ui.sectionVelocity.setVisibility(View.VISIBLE);

            MidiNote note = config.getSingleNote();
            ui.configMidiPitch.setValue(note.getPitch(), false);
            ui.configMidiVelocity.setValue(note.getVelocity(), false);

        } else if (config.hasNotes()) {

            ui.sectionData.setVisibility(View.VISIBLE);
            ui.sectionNote.setVisibility(View.GONE);
            ui.sectionVelocity.setVisibility(View.GONE);

            List<MidiNote> notes = config.getNotes();

            StringBuilder sb = new StringBuilder();

            for (MidiNote note : notes) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(note.getNoteName());
            }

            ui.txtMidiData.setText(sb.toString());

        } else {
            ui.sectionData.setVisibility(View.GONE);
            ui.sectionNote.setVisibility(View.VISIBLE);
            ui.sectionVelocity.setVisibility(View.VISIBLE);
        }

    }

    private void hideKeyboard() {
        Context context = ui.getRoot().getContext();;
        InputMethodManager imm =  (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ui.getRoot().getWindowToken(), 0);
    }

    @Override
    public void onSequencerPositionUpdate(SequencerPosition position, boolean stepChange) {
    }

    @Override
    public void onSequencerCaptureFinished(List<MidiNote> capturedNotes) {
        ElementConfig config = getConfig();
        if (null == config) {
            return;
        }

        List<MidiNote> notes = capturedNotes;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (null != config) {
                    if (null != notes && !notes.isEmpty()) {
                        config.clearNotes();
                        for (MidiNote note : notes) {
                            // map to configured midi channel
                            config.addNote(new MidiNote(config.getChannel(), note.getPitch(), note.getVelocity()));
                        }
                    } else {
                        config.reset();
                    }
                }

                ui.btnCapture.setChecked(false);
                loadConfig();
            }
        });
    }
}
