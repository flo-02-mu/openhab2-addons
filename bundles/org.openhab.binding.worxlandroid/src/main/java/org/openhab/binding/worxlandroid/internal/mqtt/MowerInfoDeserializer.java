package org.openhab.binding.worxlandroid.internal.mqtt;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MowerInfoDeserializer implements JsonDeserializer<MowerInfo> {

    @Override
    public MowerInfo deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        MowerInfo mowerInfo = new MowerInfo();
        JsonObject cfg = jsonObject.get("cfg").getAsJsonObject();
        mowerInfo.configuration = new MowerInfo.MowerConfiguration();
        mowerInfo.configuration.configId = cfg.get("id").getAsLong();
        mowerInfo.configuration.language = cfg.get("lg").getAsString();;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        ZonedDateTime dateTime = ZonedDateTime.parse(cfg.get("dt").getAsString()+ " "+cfg.get("tm").getAsString(),formatter);
        mowerInfo.configuration.dateTime = dateTime;

        MowerInfo.MowerSchedule schedule;
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
