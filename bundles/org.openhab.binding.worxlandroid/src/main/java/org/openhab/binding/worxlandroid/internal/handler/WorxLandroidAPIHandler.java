package org.openhab.binding.worxlandroid.internal.handler;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.worxlandroid.internal.restconnection.UserResponse;
import org.openhab.binding.worxlandroid.internal.restconnection.WorxLandroidRESTConnection;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.util.Collections;
import java.util.Set;

import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.*;

@NonNullByDefault
public class WorxLandroidAPIHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(WorxLandroidAPIHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_WORX_LANDROID_API);

    //private final HttpClient httpClient;

    public WorxLandroidRESTConnection getWorxLandroidRESTConnection() {
        return worxLandroidRESTConnection;
    }

    private @NonNullByDefault({}) WorxLandroidRESTConnection worxLandroidRESTConnection;

    private @NonNullByDefault({}) HttpClient httpClient;

    Configuration config;

    private @NonNullByDefault({}) KeyStore keyStore;
    private @NonNullByDefault({}) UserResponse userInfo;

    public boolean isConfigValid() {
        return configValid;
    }

    boolean configValid;

    public WorxLandroidAPIHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;

        logger.debug("Initialize WorxLandroidAccountHandler '{}'.", getThing().getUID());
        config = getConfig();

        configValid = true;
        if (StringUtils.trimToNull((String)config.get(USERNAME)) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing username");
            configValid = false;
        }
        if (StringUtils.trimToNull((String)config.get(PASSWORD)) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing password");
            configValid = false;
        }

        if (configValid) {
            worxLandroidRESTConnection = new WorxLandroidRESTConnection( this, httpClient);

                keyStore = worxLandroidRESTConnection.getKeystore();
                userInfo = worxLandroidRESTConnection.getUser();
                if(keyStore != null && userInfo != null){
                    logger.info("Successfully retrieved keystore and user info.");
                    updateStatus(ThingStatus.ONLINE);
                }else {
                    logger.error("No keystore and/or user info retrieved, cannot use bridge.");
                    updateStatus(ThingStatus.UNINITIALIZED);
                }

        }

    }

    public KeyStore getKeyStore() { return this.keyStore; }

    public UserResponse getUserInfo() {
        return this.userInfo;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    /*
    private void updateThings() {
        ThingStatus status = ThingStatus.OFFLINE;
        for (Thing thing : getThing().getThings()) {
            if (ThingStatus.ONLINE.equals(updateThing((WorxLandroidHandler) thing.getHandler(), thing))) {
                status = ThingStatus.ONLINE;
            }
        }
        updateStatus(status);
    }

    private ThingStatus updateThing(@Nullable WorxLandroidHandler handler, Thing thing) {
        if (handler != null && worxLandroidRESTConnection != null) {
            handler.updateData(worxLandroidRESTConnection);
            return thing.getStatus();
        } else {
            logger.debug("Cannot update weather data of thing '{}' as location handler is null.", thing.getUID());
            return ThingStatus.OFFLINE;
        }
    }*/

    public Configuration getWorxLandroidAPIConfig() {
        return config;
    }
}
