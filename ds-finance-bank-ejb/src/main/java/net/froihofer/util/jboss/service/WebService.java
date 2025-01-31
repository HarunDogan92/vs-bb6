package net.froihofer.util.jboss.service;

import jakarta.enterprise.context.Dependent;
import jakarta.xml.ws.BindingProvider;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import net.froihofer.dsfinance.ws.trading.api.TradingWebService;
import net.froihofer.dsfinance.ws.trading.api.TradingWebServiceService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class WebService {

    private TradingWebService tradingWebService;

    private void init() {
        if (tradingWebService == null) {
            try {
                TradingWebServiceService tradingWebServiceService = new TradingWebServiceService();
                this.tradingWebService = tradingWebServiceService.getTradingWebServicePort();
                BindingProvider bindingProvider = (BindingProvider) tradingWebService;
                bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "csdc25bb_06");
                bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "ange7fieV8Oh");
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize TradingWebService.", e);
            }
        }
    }

    public List<PublicStockQuote> getStockQuotesByName(String companyName) throws TradingWSException_Exception {
        init();
        return tradingWebService.findStockQuotesByCompanyName(companyName);
    }
    public List<PublicStockQuote> findStockQuotesByCompanyName(String partOfCompanyName) throws TradingWSException_Exception {
        init();
        return tradingWebService.findStockQuotesByCompanyName(partOfCompanyName);
    }

    public double getLastTradePriceBySymbol(String symbol) throws TradingWSException_Exception {
        init();
        try {
            List<PublicStockQuote> stockQuotes = tradingWebService.getStockQuoteHistory(symbol);

            if (stockQuotes.isEmpty()) {
                throw new IllegalStateException("No stock quotes found for symbol: " + symbol);
            }
            PublicStockQuote quote = stockQuotes.get(0);

            double price = quote.getLastTradePrice().doubleValue();
            if (price < 0) {
                throw new IllegalStateException("Negative stock price received: " + price);
            }
            return price;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch or parse the stock price", e);
        }
    }

    public void sellStock(String symbol, int shares) throws TradingWSException_Exception {
        init();
        tradingWebService.sell(symbol, shares);
    }

    public void buyStock(String symbol, int shares) throws TradingWSException_Exception {
        init();
        tradingWebService.buy(symbol, shares);
    }
}
