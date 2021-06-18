package ua.uhk.mois.financialplanning.model.entity.user;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author KVN
 * @since 15.03.2021 21:07
 */

@Data
@Entity
@Table(name = "addresses")
@ToString(exclude = "user")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Integer psc;

    @OneToOne(mappedBy = "address", optional = false)
    private User user;
}
