package org.openhab.binding.worxlandroid.internal.mqtt;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 *
 *     {"cfg":
 *         {"id":0,
 *          "lg":"it",
 *          "tm":"14:08:58",
 *          "dt":"28/09/2019",
 *          "sc":{"m":1,"p":0,"d":
 *                     [
 *                       ["14:30",0,0],["14:30",45,1],["14:30",45,0],["14:30",45,0],["14:30",45,1],["14:30",45,0],["14:30",45,0]
 *                     ]},
 *          "cmd":0, "mz":[0,0,0,0],"mzv":[0,0,0,0,0,0,0,0,0,0],"rd":180,"sn":"xxx"
 *          },
 *      "dat":{
 *          "mac":"XXXXXXXXXXXX",
 *          "fw":3.51,
 *          "bt":
 *              {"t":25.5,"v":19.98,"p":100,"nr":881,"c":0,"m":0},
 *         "dmp":[-0.8,1.1,302.6],
 *         "st":
 *              {"b":10256,"d":184891, "wt":11320},
 *         "ls":1,"le":0,"lz":0,"rsi":-62,"lk":0}}
 *
 *
 */
public class MowerInfo {
    MowerConfiguration configuration;
    MowerData data;

    public MowerConfiguration getConfiguration() {
        return configuration;
    }

    public MowerData getData() {
        return data;
    }

    public static class MowerConfiguration{
        long configId;
        String language;
        LocalDateTime dateTime;
        MowerSchedule schedule;
        int cmd;
        List<Integer> mz;  // TODO: multizone
        List<Integer> mzv; // TODO: multizone actual
        Duration rainDelay;
        String serialNumber;

        public long getConfigId() {
            return configId;
        }

        public String getLanguage() {
            return language;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public MowerSchedule getSchedule() {
            return schedule;
        }

        public int getCmd() {
            return cmd;
        }

        public List<Integer> getMz() {
            return mz;
        }

        public List<Integer> getMzv() {
            return mzv;
        }

        public Duration getRainDelay() {
            return rainDelay;
        }

        public String getSerialNumber() {
            return serialNumber;
        }
    }

    public static class MowerData{
        String mac;
        String firmware;
        Battery battery;
        List<Double> dmp; // TODO: maybe position details?
        Statistic statistic;
        int status;
        int error;
        int currentZone;
        int rsi;
        int lk;

        public String getMac() {
            return mac;
        }

        public String getFirmware() {
            return firmware;
        }

        public Battery getBattery() {
            return battery;
        }

        public List<Double> getDmp() {
            return dmp;
        }

        public Statistic getStatistic() {
            return statistic;
        }

        public int getStatus() {
            return status;
        }

        public int getError() {
            return error;
        }

        public int getCurrentZone() {
            return currentZone;
        }

        public int getRsi() {
            return rsi;
        }

        public int getLk() {
            return lk;
        }
    }

    public class MowerSchedule{
        boolean scheduleActive;
        int mowTimeExtension;
        List<MowerStart> mowerStarts;
    }

    public class MowerStart{
        String timeOfDay;
        Duration duration;
        boolean cutEdge;
    }

    public static class Battery{
        double temperature;
        double voltage;
        int percentage;
        long chargeCycle;
        boolean charging;
        int state;

        public double getTemperature() {
            return temperature;
        }

        public double getVoltage() {
            return voltage;
        }

        public int getPercentage() {
            return percentage;
        }

        public long getChargeCycle() {
            return chargeCycle;
        }

        public boolean isCharging() {
            return charging;
        }

        public int getState() {
            return state;
        }
    }

    public static class Statistic{
        Duration bladeWorkingTime;
        long distance;
        Duration workingTime;

        public Duration getBladeWorkingTime() {
            return bladeWorkingTime;
        }

        public long getDistance() {
            return distance;
        }

        public Duration getWorkingTime() {
            return workingTime;
        }
    }


}
