package org.openhab.binding.worxlandroid.internal.mqtt;

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
 *                     [["14:30",0,0],["14:30",45,1],["14:30",45,0],["14:30",45,0],["14:30",45,1],["14:30",45,0],["14:30",45,0]]},
 *          "cmd":0, "mz":[0,0,0,0],"mzv":[0,0,0,0,0,0,0,0,0,0],"rd":180,"sn":"xxx"
 *          },
 *      "dat":{
 *          "mac":"XXXXXXXXXXXX",
 *          "fw":3.51,
 *          "bt":
 *              {"t":25.5,"v":19.98,"p":100,"nr":881,"c":0,"m":0},
 *         "dmp":[-0.8,1.1,302.6],
 *         "st":{"b":10256,"d":184891, "wt":11320},
 *         "ls":1,"le":0,"lz":0,"rsi":-62,"lk":0}}
 *
 *
 */
public class MowerInfo {
    MowerConfiguration cfg;
    MowerData dat;

    public class MowerConfiguration{
        long id;
        String lg;
        String tm;
        String dt;
        MowerSchedule sc;
        int cmd;
        List<Integer> mz;
        List<Integer> mzv;
        int rd;
        String sn;

        public long getId() {
            return id;
        }

        public String getLg() {
            return lg;
        }

        public String getTm() {
            return tm;
        }

        public String getDt() {
            return dt;
        }

        public MowerSchedule getSc() {
            return sc;
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

        public int getRd() {
            return rd;
        }

        public String getSn() {
            return sn;
        }
    }

    public class MowerData{
        String mac;
        String fw;
        Battery bt;
        List<Double> dmp;
        State st;
        int ls;
        int le;
        int lz;
        int rsi;
        int lk;

        public String getMac() {
            return mac;
        }

        public String getFw() {
            return fw;
        }

        public Battery getBt() {
            return bt;
        }

        public List<Double> getDmp() {
            return dmp;
        }

        public State getSt() {
            return st;
        }

        public int getLs() {
            return ls;
        }

        public int getLe() {
            return le;
        }

        public int getLz() {
            return lz;
        }

        public int getRsi() {
            return rsi;
        }

        public int getLk() {
            return lk;
        }
    }

    public class MowerSchedule{
        int m;
        int p;
        List<MowerStart> d;
    }

    public class MowerStart{
        String timeOfDay;
        int duration;
        boolean cutEdge;
    }

    public class Battery{
        double t;
        double v;
        int p;
        long nr;
        int c;
        int m;

        public double getT() {
            return t;
        }

        public double getV() {
            return v;
        }

        public int getP() {
            return p;
        }

        public long getNr() {
            return nr;
        }

        public int getC() {
            return c;
        }

        public int getM() {
            return m;
        }
    }

    public class State{
        long b;
        long d;
        long wt;

        public long getB() {
            return b;
        }

        public long getD() {
            return d;
        }

        public long getWt() {
            return wt;
        }
    }


    public MowerConfiguration getCfg() {
        return cfg;
    }

    public MowerData getDat() {
        return dat;
    }

}
