package fr.bmartel.android.fadecandy;


public enum ServiceType {

    PERSISTENT_SERVICE(0),
    NON_PERSISTENT_SERVICE(1);

    private int mState;

    private ServiceType(int state) {
        mState = state;
    }

    public static int getState(ServiceType type) {
        switch (type) {
            case PERSISTENT_SERVICE:
                return 0;
            case NON_PERSISTENT_SERVICE:
                return 1;
        }
        return 0;
    }

    public static ServiceType getServiceType(int serviceType) {
        switch (serviceType) {
            case 0:
                return PERSISTENT_SERVICE;
            case 1:
                return NON_PERSISTENT_SERVICE;
        }
        return PERSISTENT_SERVICE;
    }
}
