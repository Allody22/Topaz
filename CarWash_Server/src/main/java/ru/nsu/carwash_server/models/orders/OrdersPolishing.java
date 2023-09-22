package ru.nsu.carwash_server.models.orders;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;


@Entity
@Table(name = "orders_polishing")
@NoArgsConstructor
@ToString
@Builder
@Getter
@Setter
@AllArgsConstructor
public class OrdersPolishing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "creation_time")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date dateOfCreation;

    @Column(nullable = false, unique = true, name = "name")
    private String name;

    @Column(name = "price_first_type")
    private int priceFirstType;

    @Column(name = "price_second_type")
    private int priceSecondType;

    @Column(name = "price_third_type")
    private int priceThirdType;

    @Column(name = "time_first_type")
    private int timeFirstType;

    @Column(name = "time_second_type")
    private int timeSecondType;

    @Column(name = "time_third_type")
    private int timeThirdType;
}
