package net.froihofer.util.jboss.service;

import jakarta.ejb.Stateless;
import net.froihofer.dsfinance.ws.trading.api.TradingWebService;
import net.froihofer.dsfinance.ws.trading.api.TradingWebServiceService;

@Stateless
public class TradingService {

    private TradingWebService tradingWebService;


}
