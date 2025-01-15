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
import net.froihofer.util.jboss.entity.TradingHistory;

import java.util.*;
import java.util.stream.Collectors;

@Stateless
@RolesAllowed({"employee", "customer"})
public class CustomerService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private WebService webService;

    private Bank getBank() {
        TypedQuery<Bank> query = em.createQuery("SELECT b FROM Bank b WHERE b.name = :name", Bank.class);
        query.setParameter("name", "Bank");
        Bank bank = query.getSingleResult();
        if (bank == null) {
            throw new IllegalStateException("Bank-Datensatz nicht gefunden!");
        }
        return bank;
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

    public Map<String, Double> getPortfolioSummary(String username) throws TradingWSException_Exception {
        List<TradingHistory> tradingHistoryList = em.createQuery(
                        "SELECT th FROM TradingHistory th WHERE th.username = :username", TradingHistory.class)
                .setParameter("username", username)
                .getResultList();

        Map<String, Integer> sharesBySymbol = tradingHistoryList.stream()
                .collect(Collectors.groupingBy(TradingHistory::getSymbol, Collectors.summingInt(TradingHistory::getShares)));

        Map<String, Double> portfolioSummary = new LinkedHashMap<>();
        double totalDepotValue = 0.0;

        for (Map.Entry<String, Integer> entry : sharesBySymbol.entrySet()) {
            String symbol = entry.getKey();
            int shares = entry.getValue();
            if (shares > 0) {
                try {
                    double currentPrice = webService.getLastTradePriceBySymbol(symbol);
                    double totalValue = currentPrice * shares;
                    portfolioSummary.put(symbol + " (" + shares + " shares)", totalValue);
                    totalDepotValue += totalValue;
                } catch (TradingWSException_Exception e) {
                    e.printStackTrace();
                }
            }
        }

        portfolioSummary.put("Total Depot Value", totalDepotValue);

        return portfolioSummary;
    }




    public void sellStock(String symbol, int shares, String username) throws TradingWSException_Exception {
        Bank bank = getBank();
        double stockPrice = webService.getLastTradePriceBySymbol(symbol);

        double totalCost = stockPrice * shares;
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

    public void buyStock(String symbol, int shares, String username) throws TradingWSException_Exception {
        Bank bank = getBank();
        double stockPrice = webService.getLastTradePriceBySymbol(symbol);

        double totalCost = stockPrice * shares;
        if (bank.getAvailableVolume() < totalCost) {
            throw new IllegalArgumentException("Nicht genügend Guthaben, um die Aktien zu kaufen!");
        }

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
