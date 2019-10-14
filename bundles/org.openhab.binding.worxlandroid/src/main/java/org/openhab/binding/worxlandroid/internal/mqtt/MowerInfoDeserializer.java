package org.openhab.binding.worxlandroid.internal.mqtt;

import com.google.gson.*;
import org.openhab.binding.worxlandroid.internal.restconnection.WorxLandroidRESTConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MowerInfoDeserializer implements JsonDeserializer<MowerInfo> {

    private final Logger logger = LoggerFactory.getLogger(MowerInfoDeserializer.class);


    @Override
    public MowerInfo deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        MowerInfo mowerInfo = new MowerInfo();
        JsonObject cfg = jsonObject.get("cfg").getAsJsonObject();
        mowerInfo.configuration = new MowerInfo.MowerConfiguration();
        mowerInfo.configuration.configId = cfg.get("id").getAsLong();
        mowerInfo.configuration.language = cfg.get("lg").getAsString();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(cfg.get("dt").getAsString()+ " "+cfg.get("tm").getAsString(),formatter);
        mowerInfo.configuration.dateTime = dateTime;

        mowerInfo.configuration.schedule = new MowerInfo.MowerSchedule();
        mowerInfo.configuration.schedule.mowTimeExtension = cfg.get("sc").getAsJsonObject().get("p").getAsInt();
        mowerInfo.configuration.schedule.scheduleActive = cfg.get("sc").getAsJsonObject().get("m").getAsBoolean();
        mowerInfo.configuration.schedule.mowerStarts = new ArrayList<>();
        //[
        // *                       ["14:30",0,0],["14:30",45,1],["14:30",45,0],["14:30",45,0],["14:30",45,1],["14:30",45,0],["14:30",45,0]
        // *                     ]
        JsonArray d = cfg.get("sc").getAsJsonObject().get("d").getAsJsonArray();
        for(DayOfWeek day : DayOfWeek.values()){
            JsonArray configForDay = d.get(day.getValue()-1).getAsJsonArray();
            logger.debug("Mower duration as int: {}",configForDay.get(1).getAsInt());
            logger.debug("Mower duration as long: {}",configForDay.get(1).getAsLong());

            mowerInfo.configuration.schedule.mowerStarts.add(
                    new MowerInfo.MowerStart(
                            configForDay.get(0).getAsString(),
                            Duration.ofMinutes(configForDay.get(1).getAsInt()),
                            configForDay.get(2).getAsBoolean()));
        }

        mowerInfo.configuration.cmd = cfg.get("id").getAsInt();
        // TODO: multizone mz
        // TODO: multizone actual mzv
        mowerInfo.configuration.rainDelay = Duration.ofMinutes(cfg.get("rd").getAsLong());
        mowerInfo.configuration.serialNumber = cfg.get("sn").getAsString();

        JsonObject dat = jsonObject.get("dat").getAsJsonObject();
        mowerInfo.data = new MowerInfo.MowerData();
        mowerInfo.data.mac = dat.get("mac").getAsString();
        mowerInfo.data.firmware = dat.get("fw").getAsString();
        JsonObject bt = dat.get("bt").getAsJsonObject();
        mowerInfo.data.battery = new MowerInfo.Battery();
        mowerInfo.data.battery.chargeCycle = bt.get("nr").getAsInt();
        mowerInfo.data.battery.charging = bt.get("c").getAsBoolean();
        mowerInfo.data.battery.percentage = bt.get("p").getAsInt();
        mowerInfo.data.battery.temperature = bt.get("t").getAsInt();
        mowerInfo.data.battery.voltage = bt.get("v").getAsInt();
        mowerInfo.data.battery.state = bt.get("m").getAsInt();

        // TODO: dmp maybe position details?
        JsonObject st = dat.get("st").getAsJsonObject();
        mowerInfo.data.statistic = new MowerInfo.Statistic();
        mowerInfo.data.statistic.bladeWorkingTime = Duration.ofMinutes(st.get("b").getAsLong());
        mowerInfo.data.statistic.workingTime = Duration.ofMinutes(st.get("wt").getAsLong());
        mowerInfo.data.statistic.distance = st.get("d").getAsLong();

        mowerInfo.data.currentZone =  dat.get("lz").getAsInt();
        mowerInfo.data.rsi =  dat.get("rsi").getAsInt();
        // TODO: lk
        mowerInfo.data.status =  dat.get("ls").getAsInt();
        mowerInfo.data.error =  dat.get("le").getAsInt();

        return mowerInfo;
    }

}
