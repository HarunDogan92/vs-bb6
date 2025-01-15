package net.froihofer.bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
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

    private String getUsername() {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getExternalContext().getUserPrincipal().getName();
    }

    public List<Holding> getHoldings() {
        return customerService.getHoldings(getUsername());
    }

    public double getStockCost(String symbol, int shares) {
        try {
            return customerService.getStockCost(symbol, shares);
        } catch (TradingWSException_Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler beim berechnen des Werts der Aktie", e.getMessage()));
        }
        return 0;
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
            FacesContext context = FacesContext.getCurrentInstance();
            String username = context.getExternalContext().getUserPrincipal().getName();
            customerService.buyStock(symbol, shares, username);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void sellStock() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String username = context.getExternalContext().getUserPrincipal().getName();
            customerService.sellStock(symbol, shares, username);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "", e.getMessage()));
        }
    }

}
