package ch.unine;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbInterface;

public class App {

    public static void main(final String[] args) throws UsbException {

        Usb4JavaHigh usb4java = new Usb4JavaHigh();
        UsbDevice usbDevice = usb4java.findDevice((short) (0x174b), (short) (0x1001));

        UsbInterface deviceInterface = usb4java.getDeviceInterface(usbDevice, 0);

        usb4java.readMessage(deviceInterface, 0);
    }
// https://stackoverflow.com/questions/28884817/usb4java-library-error-while-claiming-an-interface
    // error javax.usb.UsbPlatformException: USB error 3: Unable to claim interface: Access denied (insufficient permissions)
}
