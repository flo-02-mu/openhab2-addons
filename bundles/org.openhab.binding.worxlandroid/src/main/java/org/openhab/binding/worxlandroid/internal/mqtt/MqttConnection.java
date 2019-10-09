package org.openhab.binding.worxlandroid.internal.mqtt;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.SocketFactory;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttConnection {

    private final Logger logger = LoggerFactory.getLogger(MqttConnection.class);
    private static final String CLIENT = "android-12345"; //TODO: UUID

    private final String topicOut;
    private final String topicIn;
    private final KeyStore keyStore;
    private final String url;
    private final MqttClient mqttClient;


    public MqttConnection(KeyStore keyStore, String topicIn, String topicOut, String url) throws MqttException {
        this.keyStore = keyStore;
        this.topicIn = topicIn;
        this.topicOut = topicOut;
        this.url = "ssl://"+url;
        this.mqttClient = new MqttClient(this.url, CLIENT, new MemoryPersistence());
    }

    public void start(MqttCallback mqttCallback) throws MqttException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        logger.debug("Using keystore {}",keyStore.aliases());
        SocketFactory socketFactory = new AwsIotTlsSocketFactory(keyStore,"");

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setSocketFactory(socketFactory);

        mqttClient.connect(mqttConnectOptions);
        logger.info("Connected to Worx MQTT server {}",url);
        mqttClient.setCallback(mqttCallback);
        logger.debug("Subscribing to topic {}",topicOut);
        mqttClient.subscribe(topicOut);
    }

    public void sendMessage(String message) throws MqttException {
        mqttClient.publish(topicIn,new MqttMessage(message.getBytes()));
    }

    public void stop() throws MqttException {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            logger.error("Error while disconnecting mqtt: ",e);
        } finally {
            mqttClient.close();
        }

    }

    public boolean isConnected(){
        return mqttClient.isConnected();
    }

}
