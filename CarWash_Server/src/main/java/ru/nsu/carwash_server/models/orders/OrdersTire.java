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
@Table(name = "orders_tire")
@NoArgsConstructor
@ToString
@Getter
@Builder
@Setter
@AllArgsConstructor
public class OrdersTire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "creation_time")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date dateOfCreation;

    @Column(nullable = false, unique = true, name = "name")
    private String name;

    @Column(name = "price_r_13")
    private int price_r_13;

    @Column(name = "price_r_14")
    private int price_r_14;

    @Column(name = "price_r_15")
    private int price_r_15;

    @Column(name = "price_r_16")
    private int price_r_16;

    @Column(name = "price_r_17")
    private int price_r_17;

    @Column(name = "price_r_18")
    private int price_r_18;

    @Column(name = "price_r_19")
    private int price_r_19;

    @Column(name = "price_r_20")
    private int price_r_20;

    @Column(name = "price_r_21")
    private int price_r_21;

    @Column(name = "price_r_22")
    private int price_r_22;

    @Column(name = "time_r_13")
    private int time_r_13;

    @Column(name = "time_r_14")
    private int time_r_14;

    @Column(name = "time_r_15")
    private int time_r_15;

    @Column(name = "time_r_16")
    private int time_r_16;

    @Column(name = "time_r_17")
    private int time_r_17;

    @Column(name = "time_r_18")
    private int time_r_18;

    @Column(name = "time_r_19")
    private int time_r_19;

    @Column(name = "time_r_20")
    private int time_r_20;

    @Column(name = "time_r_21")
    private int time_r_21;

    @Column(name = "time_r_22")
    private int time_r_22;

    @Column(name = "role")
    private String role;
}
