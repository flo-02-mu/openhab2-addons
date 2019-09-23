package org.openhab.binding.worxlandroid.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.worxlandroid.internal.connection.Mower;
import org.openhab.binding.worxlandroid.internal.connection.WorxLandroidRESTConnection;
import org.openhab.binding.worxlandroid.internal.handler.WorxLandroidAPIHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.*;
import static org.openhab.binding.worxlandroid.internal.WorxLandroidHandlerFactory.SUPPORTED_THING_TYPES_UIDS;

//@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.worxlandroid")
@NonNullByDefault
public class WorxLandroidDiscoveryParticipant extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(WorxLandroidDiscoveryParticipant.class);

    private static final int DISCOVERY_TIMEOUT_SECONDS = 5;

    WorxLandroidAPIHandler worxLandroidAPIHandler;

    public WorxLandroidDiscoveryParticipant(WorxLandroidAPIHandler worxLandroidAPIHandler){
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS);
        this.worxLandroidAPIHandler = worxLandroidAPIHandler;
        logger.debug("Constructed Worx discovery service ");
    }

    @Override
    protected void startScan() {
        logger.debug("Started Worx discovery");
        if(!worxLandroidAPIHandler.isConfigValid()){
            logger.warn("Account not yet configured, postponing discovery");
            return;
        }
        WorxLandroidRESTConnection worxLandroidRESTConnection = worxLandroidAPIHandler.getWorxLandroidRESTConnection();
        List<Mower> mowers = worxLandroidRESTConnection.getMowers();
        logger.info("Found {} mowers.", mowers.size());
        for(Mower mower : mowers){
            ThingUID thingUID = new ThingUID(THING_TYPE_MOWER, mower.getSerial_number());

            Map<String, Object> properties = new HashMap<>();
            properties.put(MAC, mower.getMac_address());
            properties.put(NAME, mower.getName() );
            properties.put(FIRMWARE_VERSION,mower.getFirmware_version());

            logger.debug("Property map: {}",properties);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(thingUID)
                    .withProperties(properties)
                    .withLabel("Landroid Modell"+ mower.getName())
                    .withRepresentationProperty(mower.getSerial_number())
                    .build();

            thingDiscovered(discoveryResult);
        }
    }
}
