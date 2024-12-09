package net.froihofer.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.froihofer.util.jboss.service.BankService;

@Path("/bank")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BankResource {
    @EJB
    private BankService bankService;

    @GET
    @Path("/test")
    public String test() {
        return bankService.getTest();
    }
}
