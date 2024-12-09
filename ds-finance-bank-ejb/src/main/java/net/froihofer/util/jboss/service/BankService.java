package net.froihofer.util.jboss.service;

import jakarta.ejb.Stateless;

@Stateless
public class BankService {
    public String getTest() {
        return "Hello World";
    }
}
