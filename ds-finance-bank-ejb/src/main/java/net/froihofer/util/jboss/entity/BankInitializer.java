package net.froihofer.util.jboss.entity;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.froihofer.util.jboss.entity.Bank;

@Singleton
@Startup
public class BankInitializer {

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    public void initialize() {
        Long count = em.createQuery("SELECT COUNT(b) FROM Bank b WHERE b.name = :name", Long.class)
                .setParameter("name", "Bank")
                .getSingleResult();

        if (count == 0) {
            Bank bank = Bank.builder()
                    .name("Bank")
                    .availableVolume(1000000000)
                    .build();
            em.persist(bank);
        }
    }
}

