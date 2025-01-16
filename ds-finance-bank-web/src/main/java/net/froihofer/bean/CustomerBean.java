package net.froihofer.bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import net.froihofer.util.jboss.entity.Holding;
import net.froihofer.util.jboss.service.CustomerService;

import java.util.List;

@Named("customerBean")
@RequestScoped
@Data
public class CustomerBean {
    private String symbol;
    private int shares;

    @EJB
    private CustomerService customerService;
    @Inject
    private BankBean bankBean;

    private String getUsername() {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getExternalContext().getUserPrincipal().getName();
    }

    public List<Holding> getHoldings() {
        return bankBean.getHoldings(getUsername());
    }

    public double getTotalValue() {
        try {
            return customerService.getTotalValue(getUsername());
        } catch (TradingWSException_Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler beim berechnen des Gesamtwerts vom Depot", e.getMessage()));
        }
        return 0;
    }

    public void buyStock() {
        try {
            customerService.buyStock(symbol, shares, getUsername());
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void sellStock() {
        try {
            customerService.sellStock(symbol, shares, getUsername());
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "", e.getMessage()));
        }
    }

}
