package ch.unine;

import java.util.*;
import javax.usb.*;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import javax.usb.util.*;
import java.io.*;
public class Thermometer {

    public final static int VERNIER_VENDOR_ID = 0x174b;
    public final static int GOTEMP_PRODUCT_ID = 0x1001;

    public static void main(String[] args) throws UsbException, IOException {
        UsbDevice probe = findProbe( );
        if (probe == null) {
            System.err.println("No Go!Temp probe attached.");
            return;
        }
        UsbConfiguration config = probe.getActiveUsbConfiguration( );
        UsbInterface theInterface = config.getUsbInterface((byte) 0);

        new Thermometer().readMessage(theInterface,0);
/*
        theInterface.claim(new UsbInterfacePolicy( ) {
            public boolean forceClaim(UsbInterface usbInterface) {
                return true;
            }
        });

        UsbEndpoint endpoint = (UsbEndpoint) theInterface.getUsbEndpoints( ).get(0);

        UsbPipe pipe = endpoint.getUsbPipe( );

        // set up the IRP
        UsbIrp irp = pipe.createUsbIrp( );
        byte[] data = new byte[16];
        irp.setData(data);
        pipe.open( );
        outer: while (true) {

            pipe.syncSubmit(irp);
            int numberOfMeasurements = data[0];
            for (int i = 0; i < numberOfMeasurements; i++) {
                int result = UsbUtil.toShort(data[2*i+3], data[2*i+2]);
                int sequenceNumber = UsbUtil.unsignedInt(data[1]);
                double temperature = result / 128.0;
                if (temperature > 110.0) {
                    System.err.println("Maximum accurate temperature exceeded.");
                    break outer;
                }
                else if (temperature < -10) {
                    System.err.println("Minimum accurate temperature exceeded.");
                    break outer;
                }
                System.out.println("Measurement " + sequenceNumber + ": "
                        + temperature + "°C");
            }
            // get ready to reuse IRP
            irp.setComplete(false);

        }
        pipe.abortAllSubmissions( );
        pipe.close( );
        theInterface.release( );

        */
    }


    public void readMessage(UsbInterface iface,
                            int endPoint) {

        UsbPipe pipe = null;

        try {
            iface.claim(new UsbInterfacePolicy() {
                @Override
                public boolean forceClaim(UsbInterface usbInterface) {
                    return true;
                }
            });

            UsbEndpoint endpoint = (UsbEndpoint) iface.getUsbEndpoints().get(endPoint); // there can be more 1,2,3..
            pipe = endpoint.getUsbPipe();
            pipe.open();

            pipe.addUsbPipeListener(new UsbPipeListener() {
                @Override
                public void errorEventOccurred(UsbPipeErrorEvent event) {
                    UsbException error = event.getUsbException();
                    error.printStackTrace();
                }

                @Override
                public void dataEventOccurred(UsbPipeDataEvent event) {
                    byte[] data = event.getData();

                    System.out.println(data + " bytes received");
                    for (int i = 0; i < data.length; i++) {
                        System.out.print(data[i] + " ");
                    }
                }
            });

             byte[] data = new byte[16];
             int received = pipe.syncSubmit(data);
             System.out.println(received + " bytes received");

             pipe.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                iface.release();
            } catch (UsbClaimException e) {
                e.printStackTrace();
            } catch (UsbNotActiveException e) {
                e.printStackTrace();
            } catch (UsbDisconnectedException e) {
                e.printStackTrace();
            } catch (UsbException e) {
                e.printStackTrace();
            }
        }

    }
    private static UsbDevice findProbe( ) throws UsbException, IOException {
        UsbServices services = UsbHostManager.getUsbServices( );
        UsbHub root = services.getRootUsbHub( );
        return searchDevices(root);
    }
    private static UsbDevice searchDevices(UsbHub hub)
            throws UsbException, IOException {
        List devices = hub.getAttachedUsbDevices( );
        Iterator iterator = devices.iterator( );
        while (iterator.hasNext( )) {
            UsbDevice device = (UsbDevice) iterator.next( );
            UsbDeviceDescriptor descriptor = device.getUsbDeviceDescriptor( );
            int manufacturerCode = descriptor.idVendor( );
            int productCode = descriptor.idProduct( );
            if (manufacturerCode == VERNIER_VENDOR_ID
                    && productCode == GOTEMP_PRODUCT_ID) {
                return device;
            }
            else if (device.isUsbHub( )) {
                UsbDevice found = searchDevices((UsbHub) device);
                if (found != null) return found;
            }
        }
        return null; // didn	 find it
    }
}
