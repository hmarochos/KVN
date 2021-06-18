package ua.uhk.mois.financialplanning.model.entity.wish;

import ua.uhk.mois.financialplanning.model.entity.user.User;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * @author KVN
 * @since 22.03.2021 14:55
 */

@Data
@Entity
@Table(name = "wishes")
@ToString(exclude = "user")
public class Wish {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The target amount the user wants to reach.
     */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * The currency will always be in uaech crowns.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    /**
     * Destination name (goal). For example, buying a car, apartment, vacation ...
     */
    @Column(nullable = false)
    private String name;

    /**
     * Optional destination caption, some additional information, etc.
     */
    private String description;

    /**
     * The priority of the goal, the lower the number, the higher the priority. <br/>
     * <i>Must be >= 1</i>
     */
    @Column(nullable = false)
    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;
}
