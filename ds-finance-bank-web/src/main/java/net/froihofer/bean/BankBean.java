package net.froihofer.bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import lombok.Data;
import net.froihofer.util.jboss.service.BankService;

@Named("bankBean")
@RequestScoped
@Data
public class BankBean {
    @EJB
    private BankService bankService;
    private String symbol;
    private int shares;

    public String buyStock() {
        try {
            bankService.buyStock(symbol, shares);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

