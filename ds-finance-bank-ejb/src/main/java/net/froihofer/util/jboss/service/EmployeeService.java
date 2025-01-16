package net.froihofer.util.jboss.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import net.froihofer.util.jboss.WildflyAuthDBHelper;
import net.froihofer.util.jboss.entity.Customer;
import net.froihofer.util.jboss.entity.Depot;

import java.io.IOException;
import java.util.List;

@Stateless
@RolesAllowed({"employee"})
public class EmployeeService {
    @PersistenceContext
    private EntityManager em;

    @Inject
    private CustomerService customerService;

    private WildflyAuthDBHelper authDBHelper = new WildflyAuthDBHelper();

    public double getAvailableVolume() {
        return customerService.getBank().getAvailableVolume();
    }

    public void sellStockForCustomer(String symbol, int shares, String username) throws TradingWSException_Exception {
        customerService.sellStock(symbol, shares, username);
    }

    public void buyStockForCustomer(String symbol, int shares, String username) throws TradingWSException_Exception {
        customerService.buyStock(symbol, shares, username);
    }

    public void addCustomer(Long id, String username, String password, String firstName, String lastName, String address) throws IOException {
        Depot depot = Depot.builder().build();
        Customer customer = Customer.builder()
                .id(id)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .depot(depot)
                .address(address).build();

        em.persist(depot);
        em.persist(customer);

        authDBHelper.addUser(username, password, new String[]{"customer"});
    }


    public List<Customer> searchCustomer(Long id, String name) {
        String query = "SELECT c FROM Customer c WHERE (:id IS NULL OR c.id = :id) " +
                "AND (:name IS NULL " +
                "OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
                "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%')) " +
                "OR LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :name, '%')))";

        return em.createQuery(query, Customer.class)
                .setParameter("id", id)
                .setParameter("name", name)
                .getResultList();
    }

    //public void showportfolio (user)
}
