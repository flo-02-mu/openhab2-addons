package org.openhab.binding.worxlandroid.internal.mqtt;

public enum StatusCodeEnum {

    IDLE(0),
    HOME(1),
    START_SEQUENCE(2),
    LEAVING_HOME(3),
    FOLLOW_WIRE(4),
    SEARCHING_HOME(5),
    SEARCHING_WIRE(6),
    MOWING(7),
    MOWER_LIFTED(8),
    TRAPPED(9),
    BLADE_BLOCKED(10),
    DEBUG(11),
    REMOTE_CONTROL(12),
    GOING_HOME(30),
    ZONE_TRAINING(31),
    BORDER_CUT(32),
    SEARCHING_ZONE(33),
    PAUSE(34),
    UNKNOWN(99);

    private Integer id;

    StatusCodeEnum(int key){
        this.id = id;
    }

    public static StatusCodeEnum getById(int id) {
        for(StatusCodeEnum e : values()) {
            if(e.id.equals(id)) return e;
        }
        return UNKNOWN;
    }
}