package net.froihofer.util.jboss.repository;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import net.froihofer.util.jboss.entity.Customer;

@Stateless
@PermitAll
public class CustomerRepository {

    @PersistenceContext
    private EntityManager em;

    public Customer findByUsername(String username) {
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE c.username = :username", Customer.class);
        query.setParameter("username", username);

        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
