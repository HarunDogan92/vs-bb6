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

    private Bank getBank() {
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

    public List<Customer> searchCustomer(Long id, String name) {
        String query = "SELECT c FROM Customer c WHERE (:id IS NULL OR c.id = :id) " +
                "AND (:name IS NULL OR " +
                "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
                "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%')))";
        return em.createQuery(query, Customer.class)
                .setParameter("id", id)
                .setParameter("name", name)
                .getResultList();
    }

    public List<Holding> getPortfolioSummary(String username) {
        return getCustomerByUsername(username).getDepot().getHoldings();
    }

    public void sellStock(String symbol, int shares, String username) throws TradingWSException_Exception {
        Bank bank = getBank();
        Customer customer = getCustomerByUsername(username);
        double totalCost = getTotalCost(symbol, shares);

        holdingService.sell(symbol, shares, customer.getDepot());
        webService.sellStock(symbol, shares);
        bank.setAvailableVolume(bank.getAvailableVolume() + totalCost);
        em.merge(bank);

        FacesContext context = FacesContext.getCurrentInstance();
        String role = context.getExternalContext().isUserInRole("employee") ? "Employee" : "Customer";

        TradingHistory tradingHistory = TradingHistory.builder()
                .symbol(symbol)
                .shares(-shares)
                .availableVolume(bank.getAvailableVolume())
                .username(username)
                .role(role)
                .build();

        em.persist(tradingHistory);
    }

    public double getTotalCost(String symbol, int shares) throws TradingWSException_Exception {
        return webService.getLastTradePriceBySymbol(symbol) * shares;
    }

    public void buyStock(String symbol, int shares, String username) throws TradingWSException_Exception {
        Bank bank = getBank();
        Customer customer = getCustomerByUsername(username);
        double totalCost = getTotalCost(symbol, shares);
        if (bank.getAvailableVolume() < totalCost) {
            throw new IllegalArgumentException("Nicht genügend Guthaben, um die Aktien zu kaufen!");
        }

        holdingService.buy(symbol, shares, customer.getDepot());
        webService.buyStock(symbol, shares);
        bank.setAvailableVolume(bank.getAvailableVolume() - totalCost);
        em.merge(bank);

        FacesContext context = FacesContext.getCurrentInstance();
        String role = context.getExternalContext().isUserInRole("employee") ? "Employee" : "Customer";

        TradingHistory tradingHistory = TradingHistory.builder()
                .symbol(symbol)
                .shares(shares)
                .availableVolume(bank.getAvailableVolume())
                .username(username)
                .role(role)
                .build();

        em.persist(tradingHistory);
    }


}
