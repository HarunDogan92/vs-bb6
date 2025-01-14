package net.froihofer.util.jboss.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date tradeDate;
    private double availableVolume=1000000000;
    private String symbol;
    private int shares;
    private String username;
    private String role;

    @PrePersist
    protected void onCreate() {
        this.tradeDate = new Date();
    }
}
