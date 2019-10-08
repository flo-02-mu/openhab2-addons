package org.openhab.binding.worxlandroid.internal.mqtt;

public enum ErrorCodeEnum {

    NO_ERROR(0),
    TRAPPED(1),
    LIFTED(2),
    WIRE_MISSING(3),
    OUTSIDE_WIRE(4),
    RAINING(5),
    CLOSE_DOOR_TO_MOW(6),
    CLOSE_DOOR_TO_GO_HOME(7),
    BLADE_MOTOR_BLOCKED(8),
    WHEEL_MOTOR_BLOCKED(9),
    TRAPPED_TIMEOUT(10),
    UPSIDE_DOWN(11),
    BATTERY_LOW(12),
    REVERSE_WIRE(13),
    CHARGE_ERROR(14),
    TIMEOUT_FINDING_HOME(15),
    MOWER_LOCKED(16),
    BATTERY_OVER_TEMPERATURE(17),
    UNKNOWN(99);

    private Integer id;

    ErrorCodeEnum(int id){
        this.id = id;
    }

    public static ErrorCodeEnum getById(int id) {
        for(ErrorCodeEnum e : values()) {
            if(e.id.equals(id)) return e;
        }
        return UNKNOWN;
    }
}