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
import net.froihofer.util.jboss.entity.TradingHistory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Stateless
@RolesAllowed({"employee", "customer"})
public class BankService {
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


    public Map<String, Double> getPortfolioSummary(String username) throws TradingWSException_Exception {
        List<TradingHistory> tradingHistoryList = em.createQuery(
                        "SELECT th FROM TradingHistory th WHERE th.username = :username", TradingHistory.class)
                .setParameter("username", username)
                .getResultList();

        Map<String, Integer> sharesBySymbol = tradingHistoryList.stream()
                .collect(Collectors.groupingBy(TradingHistory::getSymbol, Collectors.summingInt(TradingHistory::getShares)));

        Map<String, Double> portfolioSummary = sharesBySymbol.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            try {
                                double currentPrice = webService.getLastTradePriceBySymbol(entry.getKey());
                                return currentPrice * entry.getValue();
                            } catch (TradingWSException_Exception e) {
                                e.printStackTrace();
                                return 0.0;
                            }
                        }
                ));

        return portfolioSummary;
    }

    public void sellStock(String symbol, int shares) throws TradingWSException_Exception {
        Bank bank = getBank();
        double stockPrice = webService.getLastTradePriceBySymbol(symbol);

        double totalCost = stockPrice * shares;
        webService.sellStock(symbol, shares);
        bank.setAvailableVolume(bank.getAvailableVolume() + totalCost);
        em.merge(bank);

        FacesContext context = FacesContext.getCurrentInstance();
        String username = context.getExternalContext().getUserPrincipal().getName();
        String role = context.getExternalContext().isUserInRole("employee") ? "employee" : "customer";

        TradingHistory tradingHistory = TradingHistory.builder()
                .symbol(symbol)
                .shares(-shares)
                .availableVolume(bank.getAvailableVolume())
                .username(username)
                .role(role)
                .build();

        em.persist(tradingHistory);
    }

    public void buyStock(String symbol, int shares) throws TradingWSException_Exception {
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
        String username = context.getExternalContext().getUserPrincipal().getName();
        String role = context.getExternalContext().isUserInRole("employee") ? "employee" : "customer";

        TradingHistory tradingHistory = TradingHistory.builder()
                .symbol(symbol)
                .shares(shares)
                .availableVolume(bank.getAvailableVolume())
                .username(username)
                .role(role)
                .build();

        em.persist(tradingHistory);
    }


    //public void sellstock

    //public void showportfolio (user)
}
