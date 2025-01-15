package net.froihofer.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import lombok.Data;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.util.jboss.entity.Customer;
import net.froihofer.util.jboss.service.CustomerService;
import net.froihofer.util.jboss.service.EmployeeService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Named("customerBean")
@RequestScoped
@Data
public class CustomerBean {
    private Long id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String address;
    private String symbol;
    private int shares;

    @EJB
    private EmployeeService employeeService;
    @EJB
    private CustomerService customerService;
    private Map<String, Double> portfolioSummary;
    private String searchQuery;
    private List<PublicStockQuote> searchResults;
    private List<Customer> searchCustomer;
    private Long customerid;
    private String customername;

    @PostConstruct
    public void init() {
        loadPortfolioSummary();
    }

    public void addCustomer() throws IOException {
        employeeService.addCustomer(id, username, password, firstName, lastName, address);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Kunde erfolgreich angelegt!"));
    }

    public void searchCustomer() {
       try {
           searchCustomer = customerService.searchCustomer(customerid, customername); // muss noch implemntiert werden
            if (searchCustomer.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Keine Ergebnisse", "Kein Kunde gefunden!"));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler bei der Suche", e.getMessage()));
            e.printStackTrace();
        }
    }


    public void searchStocks() {
        try {
            searchResults = customerService.searchStocks(searchQuery);
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
            portfolioSummary = customerService.getPortfolioSummary(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buyStock() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String username = context.getExternalContext().getUserPrincipal().getName();
            customerService.buyStock(symbol, shares, username);
            loadPortfolioSummary();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void sellStock() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String username = context.getExternalContext().getUserPrincipal().getName();
            customerService.sellStock(symbol, shares, username);
            loadPortfolioSummary();

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

}
