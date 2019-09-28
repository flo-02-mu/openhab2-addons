package org.openhab.binding.worxlandroid.internal.mqtt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class AwsSocketFactory extends SocketFactory {

    private static final String TLS_V_1_2 = "TLSv1.2";

    private final SSLSocketFactory sslSocketFactory;

    public AwsSocketFactory(KeyStore keyStore, String keyPassword) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {

            SSLContext context = SSLContext.getInstance(TLS_V_1_2);

            KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            managerFactory.init(keyStore, keyPassword.toCharArray());
            context.init(managerFactory.getKeyManagers(), null, null);

            sslSocketFactory = context.getSocketFactory();
    }


    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return sslSocketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress localHost, int localPort) throws IOException {
        return sslSocketFactory.createSocket(s, i, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int port) throws IOException {
        return sslSocketFactory.createSocket(inetAddress, port);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int port, InetAddress localAddress, int localPort) throws IOException {
        return sslSocketFactory.createSocket(inetAddress,port,localAddress,localPort);
    }
}
