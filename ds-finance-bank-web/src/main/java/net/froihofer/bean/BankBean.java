package net.froihofer.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.Getter;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.util.jboss.service.BankService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Named("bankBean")
@RequestScoped
@Data
public class BankBean {
    @EJB
    private BankService bankService;
    @Getter
    private Map<String, Double> portfolioSummary;
    private String searchQuery;
    private List<PublicStockQuote> searchResults;

    private String symbol;
    private int shares;

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
    @PostConstruct
    public void init() {
        loadPortfolioSummary();
    }

    public void searchStocks() {
        try {
            searchResults = bankService.searchStocks(searchQuery);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "fehler biem suchen", e.getMessage()));
            e.printStackTrace();
        }
    }

    public void loadPortfolioSummary() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String username = context.getExternalContext().getUserPrincipal().getName();
            portfolioSummary = bankService.getPortfolioSummary(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buyStock() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String role = context.getExternalContext().isUserInRole("employee") ? "Employee" : "Customer";
            bankService.buyStock(symbol, shares);
            loadPortfolioSummary();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            role + ": Stocks purchased successfully."));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void sellStock() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String role = context.getExternalContext().isUserInRole("employee") ? "Employee" : "Customer";
            bankService.sellStock(symbol, shares);
            loadPortfolioSummary();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            role + ": Stocks sold successfully."));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void logout() {
        try {
            System.out.println("Logout called");
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().setResponseHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            context.getExternalContext().setResponseHeader("Pragma", "no-cache");
            context.getExternalContext().setResponseHeader("Expires", "0");
            context.getExternalContext().responseSendError(401, "Unauthorized");
            context.responseComplete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
