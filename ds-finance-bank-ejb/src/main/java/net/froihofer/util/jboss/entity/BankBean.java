package net.froihofer.util.jboss.entity;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import lombok.Data;

import java.util.Base64;

@Named("bankBean")
@RequestScoped
@Data
public class BankBean {
    private String symbol;
    private int shares;

    // Methode für den REST-Call
    public String buyStock() {
        try {
            // Hier den REST-Endpunkt aufrufen
            Client client = ClientBuilder.newClient();
            String restEndpoint = "http://localhost:8080/api/bank/buy/" + symbol + "/" + shares;

            String username = "enes";
            String password = "test";
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            Response response = client.target(restEndpoint)
                    .request()
                    .header("Authorization", "Basic " + encodedAuth)
                    .post(null);

            if (response.getStatus() == 200) {
                System.out.println("Stock purchase successful!");
                return null;
            } else {
                System.out.println("Error purchasing stock: " + response.getStatus());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

