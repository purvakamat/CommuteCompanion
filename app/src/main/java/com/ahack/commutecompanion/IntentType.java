package com.ahack.commutecompanion;

/**
 * Created by Nikita Rao on 11/20/2016.
 */

public enum IntentType {
    DESTINATION_LOCATION, ROUTES;

    public static IntentType fromOrdinal(int n) {return values()[n];}
}
