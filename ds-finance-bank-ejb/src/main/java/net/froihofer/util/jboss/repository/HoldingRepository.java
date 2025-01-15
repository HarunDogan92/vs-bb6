package net.froihofer.util.jboss.repository;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import net.froihofer.util.jboss.entity.Depot;
import net.froihofer.util.jboss.entity.Holding;

@Stateless
@PermitAll
public class HoldingRepository {
    @PersistenceContext
    private EntityManager em;

    public Holding findByDepotAndSymbol(Depot depot, String symbol) {
        TypedQuery<Holding> query = em.createQuery(
                "SELECT h FROM Holding h WHERE h.depot = :depot AND h.symbol = :symbol", Holding.class);
        query.setParameter("depot", depot);
        query.setParameter("symbol", symbol);

        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null; // Gibt null zurück, wenn kein Holding gefunden wurde
        }
    }
}
