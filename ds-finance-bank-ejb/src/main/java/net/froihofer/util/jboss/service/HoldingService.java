package net.froihofer.util.jboss.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.froihofer.util.jboss.entity.Depot;
import net.froihofer.util.jboss.entity.Holding;
import net.froihofer.util.jboss.repository.HoldingRepository;

@Stateless
@RolesAllowed({"employee", "customer"})
public class HoldingService {
    @PersistenceContext
    private EntityManager em;

    @Inject
    private HoldingRepository holdingRepository;

    public Holding findByDepotAndSymbol(Depot depot, String symbol) {
        return holdingRepository.findByDepotAndSymbol(depot, symbol);
    }

    public void persist(String symbol, int shares, Depot depot) {
        Holding holding = Holding.builder()
                .symbol(symbol)
                .shares(shares)
                .depot(depot)
                .build();
        em.persist(holding);
    }

    public void buy(String symbol, int shares, Depot depot) {
        Holding holding = findByDepotAndSymbol(depot, symbol);
        if (holding == null) {
            persist(symbol, shares, depot);
        } else {
            holding.setShares(holding.getShares() + shares);
            em.merge(holding);
        }
    }

    public void sell(String symbol, int shares, Depot depot) {
        Holding holding = findByDepotAndSymbol(depot, symbol);
        if (holding == null) {
            throw new IllegalArgumentException("Holding nicht vorhanden!");
        }

        holding.setShares(holding.getShares()-shares);
        if (holding.getShares() <= 0) {
            depot.getHoldings().remove(holding);
            em.merge(depot);
        } else {
            em.merge(holding);
        }
    }
}
