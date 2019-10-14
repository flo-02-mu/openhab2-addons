package org.openhab.binding.worxlandroid.internal.mqtt;

import java.util.HashMap;
import java.util.Map;

public enum MowerCommandEnum {

    UNKNOWN(0),
    START(1),
    PAUSE(2),
    SEND_HOME(3),
    START_ZONE_TRAINING(4);

    public final Integer id;

    MowerCommandEnum(int id){
            this.id = id;
        }

    public static MowerCommandEnum getById(int id) {
        for(MowerCommandEnum e : values()) {
            if(e.id.equals(id)) return e;
        }
        return UNKNOWN;
    }

}

