package net.froihofer.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import net.froihofer.util.jboss.entity.Holding;
import net.froihofer.util.jboss.service.CustomerService;
import net.froihofer.util.jboss.service.EmployeeService;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Named("bankBean")
@SessionScoped
@Data
public class BankBean implements Serializable {
    private List<PublicStockQuote> searchResults;
    private String searchQuery;

    @EJB
    private CustomerService customerService;

    public void searchStocks() {
        try {
            searchResults = customerService.searchStocks(searchQuery);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "fehler beim suchen", e.getMessage()));
        }
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

    public List<Holding> getHoldings(String username) {
        return customerService.getHoldings(username);
    }

    public double getTotalValue(String username) {
        try {
            return customerService.getTotalValue(username);
        } catch (TradingWSException_Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler beim berechnen des Gesamtwerts vom Depot", e.getMessage()));
        }
        return 0;
    }

    public String getUsername() {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getExternalContext().getUserPrincipal() != null
                ? context.getExternalContext().getUserPrincipal().getName()
                : "Guest";
    }

    public String getUserRole() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getExternalContext().isUserInRole("employee")) {
            return "Employee";
        } else if (context.getExternalContext().isUserInRole("customer")) {
            return "Customer";
        }
        return "Unknown";
    }

    public void logout() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().invalidateSession();
            context.getExternalContext().setResponseHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            context.getExternalContext().setResponseHeader("Pragma", "no-cache");
            context.getExternalContext().setResponseHeader("Expires", "0");
            context.getExternalContext().responseSendError(401, "Unauthorized");
            context.responseComplete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
