package net.froihofer.bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;
import net.froihofer.util.jboss.entity.Customer;
import net.froihofer.util.jboss.service.EmployeeService;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Named("employeeBean")
@SessionScoped
@Data
public class EmployeeBean implements Serializable {
    private Long id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String address;
    private String symbol;
    private int shares;
    private Long customerId;
    private String customerName;
    private Customer selectedCustomer;


    @EJB
    private EmployeeService employeeService;
    @Inject
    private BankBean bankBean;
    private List<Customer> searchCustomer;

    public void addCustomer() throws IOException {
        employeeService.addCustomer(id, username, password, firstName, lastName, address);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Kunde erfolgreich angelegt!"));
    }

    public void searchCustomer() {
        try {
            searchCustomer = employeeService.searchCustomer(customerId, customerName);
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

    public void buyStockForCustomer(String username) {
        try {
            employeeService.buyStockForCustomer(symbol, shares, username);
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void sellStockForCustomer(String username) {
        try {
            employeeService.sellStockForCustomer(symbol, shares, username);
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public double getAvailableVolume() {
        return employeeService.getAvailableVolume();
    }

}
