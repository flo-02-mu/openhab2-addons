package org.openhab.binding.worxlandroid.internal.mqtt;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;

public class AwsIotTlsSocketFactory extends SSLSocketFactory{
    private static final String TLS_V_1_2 = "TLSv1.2";
    private final SSLSocketFactory sslSocketFactory;

    public AwsIotTlsSocketFactory(KeyStore keyStore, String keyPassword) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {

            SSLContext context = SSLContext.getInstance("TLSv1.2");
            KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            managerFactory.init(keyStore, keyPassword.toCharArray());
            context.init(managerFactory.getKeyManagers(), (TrustManager[])null, (SecureRandom)null);
            this.sslSocketFactory = context.getSocketFactory();

    }

    public AwsIotTlsSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public String[] getDefaultCipherSuites() {
        return this.sslSocketFactory.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return this.sslSocketFactory.getSupportedCipherSuites();
    }

    public Socket createSocket() throws IOException {
        return this.ensureTls(this.sslSocketFactory.createSocket());
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return this.ensureTls(this.sslSocketFactory.createSocket(s, host, port, autoClose));
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return this.ensureTls(this.sslSocketFactory.createSocket(host, port));
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return this.ensureTls(this.sslSocketFactory.createSocket(host, port, localHost, localPort));
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return this.ensureTls(this.sslSocketFactory.createSocket(host, port));
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return this.ensureTls(this.sslSocketFactory.createSocket(address, port, localAddress, localPort));
    }

    private Socket ensureTls(Socket socket) {
        if (socket != null && socket instanceof SSLSocket) {
            ((SSLSocket)socket).setEnabledProtocols(new String[]{"TLSv1.2"});
            SSLParameters sslParams = new SSLParameters();
            sslParams.setEndpointIdentificationAlgorithm("HTTPS");
            ((SSLSocket)socket).setSSLParameters(sslParams);
        }

        return socket;
    }
}
