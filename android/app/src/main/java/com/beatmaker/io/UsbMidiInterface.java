package com.beatmaker.io;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.beatmaker.config.Debug;
import com.beatmaker.core.midi.MidiBase;
import com.beatmaker.core.midi.MidiInterfaceBase;
import com.beatmaker.core.midi.MidiPortBase;
import com.beatmaker.core.utils.StringUtils;

public class UsbMidiInterface extends MidiInterfaceBase {

    private final UsbDevice device;
    private UsbDeviceConnection connection;
    private final int interface_index;
    private final int interface_number;
    private final UsbInterface interface_obj;

    public UsbMidiInterface() {
        super();
        device = null;
        interface_number = 0;
        interface_index = 0;
        interface_obj = null;
    }

    public UsbMidiInterface(UsbDevice usbDevice, int index, int number, UsbInterface usbInterface) {
        super();

        String id = null;
        if (null != usbDevice) {
            id = StringUtils.getSymbolName(usbDevice.getManufacturerName()) + "." + StringUtils.getSymbolName(usbDevice.getProductName()) + "." + StringUtils.getSymbolName(usbDevice.getDeviceName()) + "." + number;
        }

        this.id = id;
        this.device = usbDevice;
        this.interface_number = index;
        this.interface_index = number;
        this.interface_obj = usbInterface;

    }

    @Override
    public boolean open() {

        UsbMidi midi = (UsbMidi) MidiBase.instance();

        UsbManager usbManager = midi.getUsbManager();
        if (null == usbManager) {
            return false;
        }

        if (!Debug.MOCKUP_MIDI) {
            UsbDeviceConnection connection = usbManager.openDevice(device);
            if (null == connection) {
                return false;
            }
            connection.claimInterface(interface_obj, true);
            this.connection = connection;
        }

        this.connected = true;

        return true;
    }

    @Override
    public void close() {

        connected = false;

        if (null != connection) {
            connection.releaseInterface(interface_obj);
            connection.close();
        }

        connection = null;

        if (null != inputs) {
            inputs.clear();
            inputs = null;
        }

        if (null != outputs) {
            outputs.clear();
            outputs = null;
        }
    }

    public UsbDevice getDevice() {
        return device;
    }

    @Override
    public void setAlias(int counter) {

        String aliasBase = device.getManufacturerName() + " " + device.getProductName();

        if (0 == counter) {
            this.alias = aliasBase;
        } else {
            this.alias = aliasBase + " #" + counter;
        }
    }

    @Override
    public void detectPorts() {

        inputs = null;
        outputs = null;

        UsbInterface usbInterface = interface_obj;

        for (int j = 0; j < usbInterface.getEndpointCount(); ++j) {
            UsbEndpoint endpoint = usbInterface.getEndpoint(j);
            int direction = endpoint.getDirection();
            int type = endpoint.getType();
            addPort(new UsbMidiPort(endpoint));
        }

    }

    @Override
    public int send(MidiPortBase port, byte[] buffer, int length, int timeout) {
        UsbMidiPort p = (UsbMidiPort) port;
        return connection.bulkTransfer(p.getEndpoint(), buffer, length, timeout);
    }

    @Override
    public int receive(MidiPortBase port, byte[] buffer, int length, int timeout) {
        UsbMidiPort p = (UsbMidiPort) port;
        return connection.bulkTransfer(p.getEndpoint(), buffer, length, timeout);
    }

}
