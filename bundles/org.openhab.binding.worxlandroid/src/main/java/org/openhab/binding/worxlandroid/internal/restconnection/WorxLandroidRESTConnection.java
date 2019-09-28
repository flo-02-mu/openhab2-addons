package org.openhab.binding.worxlandroid.internal.restconnection;

import com.google.gson.Gson;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.worxlandroid.internal.handler.WorxLandroidAPIHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.eclipse.jetty.http.HttpStatus.*;
import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.PASSWORD;
import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.USERNAME;

public class WorxLandroidRESTConnection {

    private final Logger logger = LoggerFactory.getLogger(WorxLandroidRESTConnection.class);

    private static final String WORX_API_BASE = "https://api.worxlandroid.com/api/v2";
    private static final String TOKEN_ENDPOINT = WORX_API_BASE + "/oauth/token";
    private static final String ME_ENDPOINT = WORX_API_BASE+ "/users/me";
    private static final String CERTIFICATE_ENDPOINT = WORX_API_BASE + "/users/certificate";
    private static final String PRODUCT_ITEMS_ENDPOINT = WORX_API_BASE + "/product-items";

    private static final String CLIENT_SECRET = "nCH3A0WvMYn66vGorjSrnGZ2YtjQWDiCvjg7jNxK";

    private final HttpClient httpClient;
    private WorxLandroidAPIHandler worxLandroidAPIHandler;
    private String oauthToken;


    public WorxLandroidRESTConnection(WorxLandroidAPIHandler worxLandroidAPIHandler, HttpClient httpClient){
        this.worxLandroidAPIHandler = worxLandroidAPIHandler;
        this.httpClient = httpClient;
    }

    private String getOAuthToken() {

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                (String) worxLandroidAPIHandler.getWorxLandroidAPIConfig().get(USERNAME),
                (String) worxLandroidAPIHandler.getWorxLandroidAPIConfig().get(PASSWORD),
                "password",
                "1",
                "app",
                CLIENT_SECRET,
                "*");

        Request request = httpClient.newRequest(TOKEN_ENDPOINT);
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        StringContentProvider authPayload = new StringContentProvider(new Gson().toJson(authenticationRequest));
        logger.debug("Auth payload: {}",authPayload);
        request.content(authPayload, "application/json");

        ContentResponse contentResponse;
        try {
            contentResponse = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error while retrieving auth token: ",e);
            return null;
        }

        int httpStatus = contentResponse.getStatus();
        String content = contentResponse.getContentAsString();
        String errorMessage;
        logger.trace("Worx response: status = {}, content = '{}'", httpStatus, content);
        switch (httpStatus) {
            case OK_200:
                 return new Gson().fromJson(content,AuthenticationResponse.class).access_token;
            case BAD_REQUEST_400:
            case UNAUTHORIZED_401:
            case NOT_FOUND_404:
                errorMessage = content;
                logger.debug("Worx server responded with status code {}: {}", httpStatus, errorMessage);
                //throw new WorxLandroidConfigurationException(errorMessage);
            case TOO_MANY_REQUESTS_429:
            default:
                errorMessage = content;
                logger.debug("Worx server responded with status code {}: {}", httpStatus, errorMessage);
                //throw new WorxLandroidCommunicationException(errorMessage);
        }
        return null;
    }

    /**
     * Retrieve a personalized PKCS12 keystore from Worx server, used for MQTT authentication
     * @return KeyStore required for MQTT
     */
    public KeyStore getKeystore(){
        if("".equals(oauthToken) ||oauthToken == null) {
            this.oauthToken = getOAuthToken();
        }
        if(!"".equals(oauthToken)) {
            Request request = httpClient.newRequest(CERTIFICATE_ENDPOINT);
            request.method(HttpMethod.GET);
            request.header(HttpHeader.ACCEPT, "*/*");
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + oauthToken);

            logger.debug("Using token {} ",oauthToken);
            try {
                ContentResponse contentResponse = request.send();
                int httpStatus = contentResponse.getStatus();
                String content = contentResponse.getContentAsString();
                String errorMessage;
                logger.debug("Get Certificate reply: {}",content);
                switch (httpStatus) {
                    case OK_200:
                        return extractKeystore(new Gson().fromJson(content,CertificateResponse.class).pkcs12);
                    case BAD_REQUEST_400:
                    case UNAUTHORIZED_401:
                    case NOT_FOUND_404:
                        errorMessage = content;
                        logger.debug("Worx server responded with status code {}: {}", httpStatus, errorMessage);
                        //throw new WorxLandroidConfigurationException(errorMessage);
                    case TOO_MANY_REQUESTS_429:
                    default:
                        errorMessage = content;
                        logger.debug("Worx server responded with status code {}: {}", httpStatus, errorMessage);
                        //throw new WorxLandroidCommunicationException(errorMessage);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.error("Error while retrieving keystore: ",e);
            }
        }else{
            logger.error("No authentication header available!");
            return null;
        }

        return null;
    }

    private KeyStore extractKeystore(String pkcs12) {

        byte [] pkcs12Binary = Base64.getDecoder().decode(pkcs12.getBytes((StandardCharsets.UTF_8)));
        ByteArrayInputStream keyStoreInputStream = new ByteArrayInputStream(pkcs12Binary);

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreInputStream, "".toCharArray() );
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            logger.error("Cannot load keystore: ",e);
        }

        return keyStore;
    }

    public Mower[] getMowers(){

        Request request = httpClient.newRequest(PRODUCT_ITEMS_ENDPOINT);
        request.method(HttpMethod.GET);
        if("".equals(oauthToken)) {
            getOAuthToken();
        }
        if(!"".equals(oauthToken)) {
            request.header("Authorization","Bearer "+oauthToken);
            try {
                ContentResponse contentResponse = request.send();
                int httpStatus = contentResponse.getStatus();
                String content = contentResponse.getContentAsString();
                String errorMessage;
                logger.debug("Get Mowers reply: {}",content);
                switch (httpStatus) {
                    case OK_200:
                        return (new Gson().fromJson(content,Mower[].class));
                    case BAD_REQUEST_400:
                    case UNAUTHORIZED_401:
                    case NOT_FOUND_404:
                        errorMessage = content;
                        logger.debug("Worx server responded with status code {}: {}", httpStatus, errorMessage);
                        //throw new WorxLandroidConfigurationException(errorMessage);
                    case TOO_MANY_REQUESTS_429:
                    default:
                        errorMessage = content;
                        logger.debug("Worx server responded with status code {}: {}", httpStatus, errorMessage);
                        //throw new WorxLandroidCommunicationException(errorMessage);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.error("Error while retrieving keystore: ",e);
            }
        }else{
            logger.error("No authentication header available!");
            return null;
        }
        return null;
    }


    public UserResponse getUser(){

        Request request = httpClient.newRequest(ME_ENDPOINT);
        request.method(HttpMethod.GET);
        if("".equals(oauthToken)) {
            getOAuthToken();
        }
        if(!"".equals(oauthToken)) {
            request.header("Authorization","Bearer "+oauthToken);
            try {
                ContentResponse contentResponse = request.send();
                int httpStatus = contentResponse.getStatus();
                String content = contentResponse.getContentAsString();
                String errorMessage;
                logger.debug("Get users/me reply: {}",content);
                switch (httpStatus) {
                    case OK_200:
                        return (new Gson().fromJson(content,UserResponse.class));
                    case BAD_REQUEST_400:
                    case UNAUTHORIZED_401:
                    case NOT_FOUND_404:
                        errorMessage = content;
                        logger.debug("Worx server responded with status code {}: {}", httpStatus, errorMessage);
                        //throw new WorxLandroidConfigurationException(errorMessage);
                    case TOO_MANY_REQUESTS_429:
                    default:
                        errorMessage = content;
                        logger.debug("Worx server responded with status code {}: {}", httpStatus, errorMessage);
                        //throw new WorxLandroidCommunicationException(errorMessage);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.error("Error while retrieving user info: ",e);
            }
        }else{
            logger.error("No authentication header available!");
            return null;
        }
        return null;
    }

}
