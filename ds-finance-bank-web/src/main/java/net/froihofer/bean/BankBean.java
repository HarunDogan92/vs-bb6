package net.froihofer.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import net.froihofer.util.jboss.entity.Holding;
import net.froihofer.util.jboss.service.CustomerService;
import net.froihofer.util.jboss.service.EmployeeService;

import java.io.IOException;
import java.util.List;

@Named("bankBean")
@RequestScoped
@Data
public class BankBean {
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
