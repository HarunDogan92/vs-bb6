package net.froihofer.bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.util.jboss.entity.Holding;
import net.froihofer.util.jboss.service.CustomerService;
import net.froihofer.util.jboss.service.EmployeeService;

import java.util.List;

@Named("customerBean")
@RequestScoped
@Data
public class CustomerBean {
    private String symbol;
    private int shares;
    private List<PublicStockQuote> searchResults;
    private String searchQuery;

    @EJB
    private EmployeeService employeeService;
    @EJB
    private CustomerService customerService;

    public List<Holding> getHoldings() {
        FacesContext context = FacesContext.getCurrentInstance();
        String username = context.getExternalContext().getUserPrincipal().getName();
        return customerService.getHoldings(username);
    }

    public void searchStocks() {
        try {
            searchResults = customerService.searchStocks(searchQuery);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "fehler beim suchen", e.getMessage()));
            e.printStackTrace();
        }
    }

    public void buyStock() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String username = context.getExternalContext().getUserPrincipal().getName();
            customerService.buyStock(symbol, shares, username);
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
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

}
