package org.openhab.binding.worxlandroid.internal.restconnection;

public class AuthenticationRequest {
    String username;
    String password;
    String grant_type;
    String client_id;
    String type;
    String client_secret;
    String scope;

    public AuthenticationRequest(String username, String password, String grant_type, String client_id, String type, String client_secret, String scope) {
        this.username = username;
        this.password = password;
        this.grant_type = grant_type;
        this.client_id = client_id;
        this.type = type;
        this.client_secret = client_secret;
        this.scope = scope;
    }
}
