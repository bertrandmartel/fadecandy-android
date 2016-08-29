package fr.bmartel.android.fadecandy;

/**
 * Created by akinaru on 29/08/16.
 */
public interface IFadecandyListener {

    void onServerStart();

    void onServerClose();

    void onServerError(ServerError error);
}
