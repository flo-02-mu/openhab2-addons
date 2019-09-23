package org.openhab.binding.worxlandroid.internal.handler;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.worxlandroid.internal.connection.WorxLandroidRESTConnection;
import org.openhab.binding.worxlandroid.internal.discovery.WorxLandroidDiscoveryParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.*;

@NonNullByDefault
public class WorxLandroidAPIHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(WorxLandroidAPIHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_WORX_LANDROID_API);

    private final HttpClient httpClient;

    public WorxLandroidRESTConnection getWorxLandroidRESTConnection() {
        return worxLandroidRESTConnection;
    }

    private @NonNullByDefault({}) WorxLandroidRESTConnection worxLandroidRESTConnection;

    Configuration config;
    private @NonNullByDefault({}) KeyStore keyStore;

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
            worxLandroidRESTConnection = new WorxLandroidRESTConnection( httpClient,this);

                keyStore = worxLandroidRESTConnection.getKeystore();
                if(keyStore != null){
                    logger.info("Successfully retrieved keystore.");
                    updateStatus(ThingStatus.ONLINE);
                    WorxLandroidDiscoveryParticipant worxLandroidDiscoveryParticipant = new WorxLandroidDiscoveryParticipant(this);
                }else {
                    logger.error("No keystore retrieved, cannot use bridge.");
                    updateStatus(ThingStatus.UNINITIALIZED);
                }

            /*
            if (refreshJob == null || refreshJob.isCancelled()) {
                logger.debug("Start refresh job at interval {} min.", refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateThings, INITIAL_DELAY_IN_SECONDS,
                        TimeUnit.MINUTES.toSeconds(refreshInterval), TimeUnit.SECONDS);
            }
             */
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    public Configuration getWorxLandroidAPIConfig() {
        return config;
    }
}
