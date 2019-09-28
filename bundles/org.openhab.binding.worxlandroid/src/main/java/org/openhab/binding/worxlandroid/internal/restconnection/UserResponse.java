package org.openhab.binding.worxlandroid.internal.restconnection;

import java.math.BigDecimal;

public class UserResponse {
    long id;
    String email;
    String name;
    String surname;
    String user_type;
    String locale;
    boolean push_notifications;
    Location location;
    boolean terms_of_use_agreed;
    int country_id;
    String mqtt_endpoint;
    String actions_on_google_pin_code;
    String created_at;
    String updated_at;

    public String getMqtt_endpoint() {
        return this.mqtt_endpoint;
    }

    class Location {
        BigDecimal latitude;
        BigDecimal longitude;
    }
}
