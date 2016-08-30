package fr.bmartel.android.fadecandy;

import android.os.Binder;

import fr.bmartel.android.fadecandy.service.FadecandyService;


public class FadecandyServiceBinder extends Binder {

    private FadecandyService fadecandyService;

    public FadecandyServiceBinder(FadecandyService fadecandyService) {
        this.fadecandyService = fadecandyService;
    }

    /**
     * @return a reference to the Service
     */
    public FadecandyService getService() {
        return fadecandyService;
    }
}
