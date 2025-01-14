package net.froihofer.bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import lombok.Data;
import net.froihofer.util.jboss.service.EmployeeService;

import java.io.IOException;

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

    @EJB
    private EmployeeService employeeService;

    public void addCustomer() throws IOException {
        employeeService.addCustomer(id, username, password, firstName, lastName, address);
    }
}
