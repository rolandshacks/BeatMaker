package com.beatmaker.io;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbEndpoint;

import com.beatmaker.core.midi.MidiPortBase;

public class UsbMidiPort extends MidiPortBase {

    private final UsbEndpoint endpoint;

    public UsbMidiPort() {
        super();
        this.endpoint = null;
    }

    public UsbMidiPort(UsbEndpoint endpoint) {
        super();

        assert (null != endpoint);

        this.endpoint = endpoint;

        if (null != endpoint) {
            int endpoint_direction = endpoint.getDirection();

            if (UsbConstants.USB_DIR_IN == endpoint_direction) {
                this.direction = MidiPortBase.INPUT;
            } else if (UsbConstants.USB_DIR_OUT == endpoint_direction) {
                this.direction = MidiPortBase.OUTPUT;
            }
        }
    }

    @Override
    public int getMaxPacketSize() {
        if (null == endpoint) {
            return 0;
        }

        return endpoint.getMaxPacketSize();
    }

    public UsbEndpoint getEndpoint() {
        return endpoint;
    }
}
