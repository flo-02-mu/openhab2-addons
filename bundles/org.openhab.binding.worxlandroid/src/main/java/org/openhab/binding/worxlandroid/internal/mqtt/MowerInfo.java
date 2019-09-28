package org.openhab.binding.worxlandroid.internal.mqtt;

import java.util.List;

public class MowerInfo {
    /*
    {"cfg":{"id":0,"lg":"it","tm":"14:08:58","dt":"28/09/2019",
            "sc":{"m":1,"p":0,"d":
                    [["14:30",0,0],["14:30",45,1],["14:30",45,0],["14:30",45,0],["14:30",45,1],["14:30",45,0],["14:30",45,0]]},
            "cmd":0, "mz":[0,0,0,0],"mzv":[0,0,0,0,0,0,0,0,0,0],"rd":180,"sn":"20183019090500296782"},
        "dat":{"mac":"F0FE6BB195F6","fw":3.51,
            "bt":{"t":25.5,"v":19.98,"p":100,"nr":881,"c":0,"m":0},
        "dmp":[-0.8,1.1,302.6],
        "st":{"b":10256,"d":184891, "wt":11320},
        "ls":1,"le":0,"lz":0,"rsi":-62,"lk":0}}

    */
    MowerConfiguration cfg;
    MowerData dat;

    public class MowerConfiguration{
        long id;
        String lg;
        String tm;
        String dt;
        MowerSchedule sc;

    }

    public class MowerData{
        String timeOfDay;
        int duration;
        boolean cutEdge;
    }

    public class MowerSchedule{
        int m;
        int p;
        List<MowerStart> d;
    }

    public class MowerStart{

    }
}
