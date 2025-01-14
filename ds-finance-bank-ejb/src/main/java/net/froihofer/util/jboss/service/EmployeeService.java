package net.froihofer.util.jboss.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;

@Stateless
@RolesAllowed({"employee"})
public class EmployeeService {
    @PersistenceContext
    private EntityManager em;

    @Inject
    private WebService webService;

    @Inject
    private CustomerService customerService;


    public void sellStockForCustomer(String symbol, int shares, String username) throws TradingWSException_Exception {
        customerService.sellStock(symbol, shares, username);
    }

    public void buyStockForCustomer(String symbol, int shares, String username) throws TradingWSException_Exception {
        customerService.buyStock(symbol, shares, username);
    }


    //public void showportfolio (user)
}
