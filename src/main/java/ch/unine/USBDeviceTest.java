package ch.unine;

import java.util.List;

import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfaceDescriptor;
import javax.usb.UsbIrp;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;

public class USBDeviceTest {

    private static void dump(UsbDevice device)
    {
        UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
        System.out.format("%04x:%04x%n", desc.idVendor() & 0xffff, desc.idProduct() & 0xffff);
        if (device.isUsbHub())
        {
            UsbHub hub = (UsbHub) device;
            for (UsbDevice child : (List<UsbDevice>) hub.getAttachedUsbDevices())
            {
                dump(child);
            }
        }
    }

    public static void main(String[] args) throws UsbException
    {
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub rootHub = services.getRootUsbHub();
        //dump(rootHub);
        USBDeviceTest usb=new USBDeviceTest();
        UsbDevice device=usb.findDevice(rootHub, (short)0x174b, (short)0x1001);
        try{
            System.out.println(device);
            List<UsbConfiguration> configs=device.getUsbConfigurations();
            System.out.println(configs.size());
            UsbConfiguration config=null;

            for(UsbConfiguration con:configs){
                if(con.isActive()){
                    config=con;
                    break;
                }
            }

            if(!config.isActive()){
                System.out.println("config is not active");
            }
            List<UsbInterface> interfaces=config.getUsbInterfaces();
            if(interfaces.size()>0){
                UsbInterface interf = interfaces.get(0);
                UsbInterfaceDescriptor idesc=interf.getUsbInterfaceDescriptor();
                System.out.println("interface type:"+idesc.bNumEndpoints());
                if(!interf.isActive()){
                    System.out.println("interface is not active");
                }

                if(!interf.isClaimed()){
                    System.out.println("need claim");
                    interf.claim();
                }

                List<UsbEndpoint> points=interf.getUsbEndpoints();
                System.out.println("point count:"+points.size());
                if(points.size()>0){
                    UsbEndpoint endPoint=points.get(0);
                    System.out.println("dirct:"+endPoint.getDirection());

                    UsbPipe pipe=endPoint.getUsbPipe();

                    if(!pipe.isOpen()){
                        System.out.println("need open pipe");
                        pipe.open();
                    }

                    pipe.addUsbPipeListener(new UsbPipeListener() {

                        @Override
                        public void errorEventOccurred(UsbPipeErrorEvent event) {
                            System.out.println("error occur");
                            System.out.println(event.getUsbException());

                        }

                        @Override
                        public void dataEventOccurred(UsbPipeDataEvent event) {
                            System.out.println("data occur");

                        }
                    });

/*
                    byte[] buf=new byte[255];
                    int len=pipe.syncSubmit(buf);    // here get exception*/
                    final byte[] bytes = new byte[64];
                    bytes[0] = (byte)0x04;
                    bytes[1] = (byte)0x20;
                    //pipe.syncSubmit(bytes);

                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }


    public UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
    {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub())
            {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }
}