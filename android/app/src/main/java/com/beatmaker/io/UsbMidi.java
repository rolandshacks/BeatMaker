package com.beatmaker.io;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.beatmaker.app.Application;
import com.beatmaker.core.application.ApplicationBase;
import com.beatmaker.core.midi.MidiApiProvider;
import com.beatmaker.core.midi.MidiBase;
import com.beatmaker.core.midi.MidiInterfaceBase;
import com.beatmaker.core.utils.Logger;

import java.util.HashMap;

public class UsbMidi extends MidiBase implements MidiApiProvider {

    private static final String TAG = "Midi";
    private static final String USB_PERMISSION_GRANTED_ACTION = "BEATMAKER.USB_PERMISSION_GRANTED_ACTION";
    private static final int MIDI_SUBCLASS = 3;

    private UsbManager usbManager;
    private Context context;

    private HashMap<String, UsbMidiInterface> midiInterfaces = new HashMap<>();

    class Receiver extends BroadcastReceiver {

        private final UsbMidiInterface midiInterface;

        public Receiver(UsbMidiInterface midiInterface) {
            this.midiInterface = midiInterface;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(USB_PERMISSION_GRANTED_ACTION)) {
                return;
            }

            if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                onAccessDenied(midiInterface);
                return;
            }

            onInterfaceReady(midiInterface, true);
        }
    }

    public UsbMidi(Context context) {
        super();
        this.context = context;
    }

    public UsbManager getUsbManager()  {
        return usbManager;
    }

    @Override
    public synchronized void create() {
        super.create();
        Application app = (Application) ApplicationBase.instance();
        usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
    }

    @Override
    public synchronized void destroy() {
        super.destroy();
    }

    protected HashMap<String, MidiInterfaceBase> queryMidiInterfaces() {
        HashMap<String, MidiInterfaceBase> interfaces = new HashMap<>();

        HashMap<String, UsbDevice> deviceMap = usbManager.getDeviceList();
        if (deviceMap.size() == 0) {
            Logger.d(TAG, "no USB devices found");
        }

        for (UsbDevice device : deviceMap.values()) {

            int midiInterfaceCount = 0;

            for (int interfaceId = 0; interfaceId < device.getInterfaceCount(); ++interfaceId) {
                UsbInterface usbInterface = device.getInterface(interfaceId);
                if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_AUDIO && usbInterface.getInterfaceSubclass() == MIDI_SUBCLASS) {
                    MidiInterfaceBase midiInterface = new UsbMidiInterface(device, interfaceId, midiInterfaceCount, usbInterface);
                    interfaces.put(midiInterface.getId(), midiInterface);
                    midiInterfaceCount++;
                }
            }
        }

        return interfaces;
    }

    @Override
    protected void connect(MidiInterfaceBase midiInterface) {
        Logger.d(TAG, "connect midi interface: " + midiInterface.getAlias());

        if (!(midiInterface instanceof UsbMidiInterface)) return;

        UsbMidiInterface intf = (UsbMidiInterface) midiInterface;

        PendingIntent intent = PendingIntent.getBroadcast(context, 0, new Intent(USB_PERMISSION_GRANTED_ACTION), 0);
        context.registerReceiver(new Receiver(intf), new IntentFilter(USB_PERMISSION_GRANTED_ACTION));
        usbManager.requestPermission(intf.getDevice(), intent);

        Logger.d(TAG, "USB device access permission request started");
    }

    private void onAccessDenied(UsbMidiInterface midiInterface) {
        Logger.d(TAG, "USB device access permission denied");
        stop();
    }

}
