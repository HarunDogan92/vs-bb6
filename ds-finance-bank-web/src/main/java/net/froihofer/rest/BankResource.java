package net.froihofer.rest;

import jakarta.annotation.ManagedBean;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import net.froihofer.util.jboss.service.BankService;

import java.beans.JavaBean;
import java.util.List;

@Path("/bank")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped      // Die Bean lebt während einer HTTP-Anfrage
public class BankResource {
    @EJB
    private BankService bankService;

    @GET
    @Path("/test")
    public String test() {
        return bankService.getTest();
    }

    @GET
    @Path("/public_stock_quotes/{companyName}")
    public List<PublicStockQuote> getPublicStockQuotes(@PathParam("companyName") String companyName) throws TradingWSException_Exception {
        return bankService.getPublicStockQuotes(companyName);
    }

    @POST
    @Path("/buy/{symbol}/{shares}")
    public Response buyStock(@PathParam("symbol") String symbol, @PathParam("shares") int shares) throws TradingWSException_Exception {
        bankService.buyStock(symbol, shares);
        return Response.ok().build();
    }
}
