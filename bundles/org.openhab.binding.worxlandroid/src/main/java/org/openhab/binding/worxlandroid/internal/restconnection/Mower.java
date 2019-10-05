package org.openhab.binding.worxlandroid.internal.restconnection;

import java.math.BigDecimal;

public class Mower {
    long id;
    int product_it;
    long user_id;
    String serial_number;
    String mac_address;
    String name;
    Location setup_location;
    boolean locked;
    String firmware_version;
    boolean firmware_auto_upgrade;
    long distance_covered;
    long mower_work_time;
    long blade_work_time;
    int battery_charge_cycles;
    long messages_in;
    long messages_out;
    boolean push_notifications;
    City city;
    String sim;
    String push_notifications_level;
    int lawn_size;
    String lawn_perimeter;
    long raw_messages_in;
    long raw_messages_out;
    int test;
    boolean iot_registered;
    boolean warranty_registered;
    String pin_code;
    String time_zone;
    String purchased_at;
    String warranty_expires_at;
    String registered_at;
    boolean online;
    String app_settings;
    String accessories;
    String auto_schedule_settings;
    boolean auto_schedule;
    MqttTopics mqtt_topics;
    String created_at;
    String updated_at;

    public String getSerial_number() {
        return serial_number;
    }

    public String getMac_address() {
        return mac_address;
    }

    public String getName() {
        return name;
    }

    public String getFirmware_version() {
        return firmware_version;
    }

    public MqttTopics getMqtt_topics() {
        return this.mqtt_topics;
    }

    public boolean isOnline() {
        return online;
    }

    class Location{
        BigDecimal latitude;
        BigDecimal longitude;
    }

    class City {
        long id;
        int country_id;
        String name;
        BigDecimal latitude;
        BigDecimal longitude;
        String created_at;
        String updated_at;
    }

    public class MqttTopics{
        String command_in;
        String command_out;

        public String getCommand_in() {
            return this.command_in;
        }

        public String getCommand_out() {
            return this.command_out;
        }


    }
}
