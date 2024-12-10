package net.froihofer.util.jboss.service;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import net.froihofer.util.jboss.entity.Bank;
import net.froihofer.util.jboss.entity.TradingHistory;

import java.util.List;

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
        return query.getSingleResult();
    }

    public String getTest() {
        return webService.getTest();
    }

    public List<PublicStockQuote> getPublicStockQuotes(String companyName) throws TradingWSException_Exception {
        return webService.getStockQuotesByName(companyName);
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

        TradingHistory tradingHistory = TradingHistory.builder()
                .symbol(symbol)
                .shares(shares)
                .availableVolume(bank.getAvailableVolume())
                .build();
        em.persist(tradingHistory);
    }
}
