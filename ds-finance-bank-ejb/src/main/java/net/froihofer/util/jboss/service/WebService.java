package net.froihofer.util.jboss.service;

import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.xml.ws.BindingProvider;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import net.froihofer.dsfinance.ws.trading.api.TradingWebService;
import net.froihofer.dsfinance.ws.trading.api.TradingWebServiceService;

import java.util.List;

@Dependent
public class WebService {

    private TradingWebService tradingWebService;

    private void init() {
        TradingWebServiceService tradingWebServiceService = new TradingWebServiceService();
        this.tradingWebService = tradingWebServiceService.getTradingWebServicePort();
        BindingProvider bindingProvider = (BindingProvider) tradingWebService;
        bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "csdc25bb_06");
        bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "ange7fieV8Oh");
    }

    public String getTest() {
        return "Hello World";
    }

    public List<PublicStockQuote> getStockQuotesByName(String companyName) throws TradingWSException_Exception {
        init();
        return tradingWebService.findStockQuotesByCompanyName(companyName);
    }

    public double getLastTradePriceBySymbol(String symbol) throws TradingWSException_Exception {
        init();
        return tradingWebService.getStockQuotes(List.of(symbol)).get(0).getLastTradePrice().doubleValue();
    }

    public void buyStock(String symbol, int shares) throws TradingWSException_Exception {
        init();
        tradingWebService.buy(symbol, shares);
    }
}
