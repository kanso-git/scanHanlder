package ch.unine;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.swing.JOptionPane;

import org.usb4java.BufferUtils;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

public class Sample {
    private static byte endpoint;
    // private static final byte IN_ENDPOINT = 0;
    private static Device device;
    private static   Context context = new Context();
    private static DeviceHandle handle;

    public static void main(String[] args) {

        // Create the libusb context
        LibUsb.init(null);
        findDevice(context, (short) (0x174b), (short) (0x1001));
        // Deinitialize the libusb context
        LibUsb.exit(context);

    }

    public static void findDevice(Context context, short vendorId, short productId) {

        // Initialize the libusb context
        int result = LibUsb.init(context);
        if (result < 0) {
            throw new LibUsbException("Unable to initialize libusb", result);
        }

        // Read the USB device list
        DeviceList list = new DeviceList();
        result = LibUsb.getDeviceList(context, list);
        if (result < 0) {
            throw new LibUsbException("Unable to get device list", result);
        }

        try {
            // Iterate over all devices and list them
            for (Device device : list) {

                int address = LibUsb.getDeviceAddress(device);
                int busNumber = LibUsb.getBusNumber(device);
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);

                if (result < 0) {
                    throw new LibUsbException(
                            "Unable to read device descriptor", result);
                }
                System.out.format(
                        "Bus %03d, Device %03d: Vendor %04x, Product %04x%n",
                        busNumber, address, descriptor.idVendor(),
                        descriptor.idProduct());

                if (result != LibUsb.SUCCESS) {
                    throw new LibUsbException("Unable to read device descriptor", result);
                }
                if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) {

                    System.out.println("Device Found");
                    getDeviceHandle(device);
                    LibUsb.claimInterface(handle, 0);
                }

            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }
    }

    public static void getDeviceHandle(Device device) {
        endpoint = (byte)LibUsb.getDeviceAddress(device);
        handle = new DeviceHandle();
        JOptionPane.showMessageDialog(null, "endpoint="+endpoint);

        int result = LibUsb.open(device, handle);


        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to open USB device", result);
        }

        try {
            // Use device handle here
            claimDevice(handle, 0);


        } finally {
            LibUsb.close(handle);
        }
    }

    public static void claimDevice(DeviceHandle handle, int interfaceNumber) {


        int result = LibUsb.claimInterface(handle, interfaceNumber);

        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to claim interface", result);
        }
        try {

            System.out.println("Device Claimed");
            //sendData(handle);
            read(handle,1000);
            JOptionPane.showMessageDialog(null, "interface="+interfaceNumber);
            read(handle, 2048);






        } finally {
            result = LibUsb.releaseInterface(handle, interfaceNumber);
            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Unable to release interface", result);
            }
        }
    }

    @SuppressWarnings("unused")







    public static void sendData(DeviceHandle handle) {

        char[] initEP = new char[]{0x1b, '@'};
        char[] cutPaper = new char[]{0x1d, 'V', 1};

        String initStr = new String(initEP);
        String cutStr = new String(cutPaper);
        String text = "blabla \n\n\n";

        ByteBuffer buffer = ByteBuffer.allocateDirect(8);
        buffer.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        // SEND INIT BYTE
        int transfered = LibUsb.controlTransfer(handle,
                (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE),
                (byte) 0x01, (short) 2, (short) 1, buffer, 5000);

        if (transfered < 0) {
            throw new LibUsbException("Control transfer failed", transfered);
        }

        // SENDT TEXT
        buffer = ByteBuffer.wrap(text.getBytes());

        transfered = LibUsb.controlTransfer(handle,
                (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE),
                (byte) 0x01, (short) 2, (short) 1, buffer, 5000);

        if (transfered < 0) {
            throw new LibUsbException("Control transfer failed", transfered);
        }

      /*  // SENDT CUT BYTE
        buffer = ByteBuffer.wrap(text.getBytes());

        transfered = LibUsb.controlTransfer(handle,
                (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE),
                (byte) 0x09, (short) 2, (short) 1, buffer, 5000);

        if (transfered < 0) {
            throw new LibUsbException("Control transfer failed", transfered);
        } */
        System.out.println(transfered + " bytes sent");
    }

    public static ByteBuffer read(DeviceHandle handle, int size)
    {


        ByteBuffer buffer = BufferUtils.allocateByteBuffer(size).order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer transferred = BufferUtils.allocateIntBuffer();

        // IntBuffer transferred = BufferUtils.allocateIntBuffer();
        long TIMEOUT = 5000;


        JOptionPane.showMessageDialog(null, "endpoint_in="+ LibUsb.ENDPOINT_IN);
        JOptionPane.showMessageDialog(null, "endpoint_out="+ LibUsb.ENDPOINT_OUT);
        JOptionPane.showMessageDialog(null, "handle="+handle);

        int result = LibUsb.bulkTransfer(handle, (byte)-128, buffer,
                transferred, TIMEOUT);

        JOptionPane.showMessageDialog(null, result);

        if (result != LibUsb.SUCCESS)
        {
            //throw new LibUsbException("Unable to read data", result);
        }
        else{System.out.println(transferred.get() + " bytes read from device");

        }


        return buffer;
    }
}