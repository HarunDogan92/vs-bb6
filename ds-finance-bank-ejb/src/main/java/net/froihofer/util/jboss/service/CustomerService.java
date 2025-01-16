package net.froihofer.util.jboss.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import net.froihofer.util.jboss.entity.Bank;
import net.froihofer.util.jboss.entity.Customer;
import net.froihofer.util.jboss.entity.Holding;
import net.froihofer.util.jboss.entity.TradingHistory;
import net.froihofer.util.jboss.repository.CustomerRepository;

import java.util.*;

@Stateless
@RolesAllowed({"employee", "customer"})
public class CustomerService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private WebService webService;

    @Inject
    private CustomerRepository customerRepository;

    @Inject
    private HoldingService holdingService;

    public Bank getBank() {
        TypedQuery<Bank> query = em.createQuery("SELECT b FROM Bank b WHERE b.name = :name", Bank.class);
        query.setParameter("name", "Bank");
        Bank bank = query.getSingleResult();
        if (bank == null) {
            throw new IllegalStateException("Bank-Datensatz nicht gefunden!");
        }
        return bank;
    }

    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username);
    }

    public List<PublicStockQuote> getPublicStockQuotes(String companyName) throws TradingWSException_Exception {
        return webService.getStockQuotesByName(companyName);
    }

    public List<PublicStockQuote> searchStocks(String companyName) throws TradingWSException_Exception {
        return webService.findStockQuotesByCompanyName(companyName);
    }

    public List<Holding> getHoldings(String username) {
        return getCustomerByUsername(username).getDepot().getHoldings();
    }

    public double getTotalValue(String username) throws TradingWSException_Exception {
        double totalCost = 0;
        for (Holding holding : getHoldings(username)) {
            totalCost+=getStockCost(holding.getSymbol(), holding.getShares());
        }
        return totalCost;
    }

    public void sellStock(String symbol, int shares, String username) throws TradingWSException_Exception {
        Bank bank = getBank();
        String symbolUpper = symbol.toUpperCase();
        Customer customer = getCustomerByUsername(username);
        double stockCost = getStockCost(symbolUpper, shares);

        holdingService.sell(symbolUpper, shares, customer.getDepot());
        webService.sellStock(symbolUpper, shares);
        bank.setAvailableVolume(bank.getAvailableVolume() + stockCost);
        em.merge(bank);

        FacesContext context = FacesContext.getCurrentInstance();
        String role = context.getExternalContext().isUserInRole("employee") ? "Employee" : "Customer";

        TradingHistory tradingHistory = TradingHistory.builder()
                .symbol(symbolUpper)
                .shares(-shares)
                .price(stockCost)
                .availableVolume(bank.getAvailableVolume())
                .username(username)
                .role(role)
                .build();

        em.persist(tradingHistory);
    }

    public double getStockCost(String symbol, int shares) throws TradingWSException_Exception {
        return webService.getLastTradePriceBySymbol(symbol) * shares;
    }

    public void buyStock(String symbol, int shares, String username) throws TradingWSException_Exception {
        Bank bank = getBank();
        String symbolUpper = symbol.toUpperCase();
        Customer customer = getCustomerByUsername(username);
        double stockCost = getStockCost(symbolUpper, shares);
        if (bank.getAvailableVolume() < stockCost) {
            throw new IllegalArgumentException("Nicht genügend Guthaben, um die Aktien zu kaufen!");
        }

        holdingService.buy(symbolUpper, shares, customer.getDepot());
        webService.buyStock(symbolUpper, shares);
        bank.setAvailableVolume(bank.getAvailableVolume() - stockCost);
        em.merge(bank);

        FacesContext context = FacesContext.getCurrentInstance();
        String role = context.getExternalContext().isUserInRole("employee") ? "Employee" : "Customer";

        TradingHistory tradingHistory = TradingHistory.builder()
                .symbol(symbolUpper)
                .shares(shares)
                .price(stockCost)
                .availableVolume(bank.getAvailableVolume())
                .username(username)
                .role(role)
                .build();

        em.persist(tradingHistory);
    }


}
