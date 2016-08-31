package fr.bmartel.android.fadecandy.inter;

import fr.bmartel.android.fadecandy.model.UsbItem;

public interface IUsbListener {

    void onUsbDeviceAttached(UsbItem usbItem);

    void onUsbDeviceDetached(UsbItem usbItem);
}
