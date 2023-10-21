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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import java.util.Date;


@Entity
@Table(name = "orders_washing",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name"})
        }
)
@NoArgsConstructor
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrdersWashing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "creation_time")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date dateOfCreation;

    @Column(nullable = false, unique = true, name = "name")
    private String name;

    @Min(0)
    @Column(name = "price_first_type")
    private int priceFirstType;

    @Min(0)
    @Column(name = "price_second_type")
    private int priceSecondType;

    @Min(0)
    @Column(name = "price_third_type")
    private int priceThirdType;

    @Min(0)
    @Column(name = "time_first_type")
    private int timeFirstType;

    @Min(0)
    @Column(name = "time_second_type")
    private int timeSecondType;

    @Min(0)
    @Column(name = "time_third_type")
    private int timeThirdType;

    @Column(name = "role")
    private String role;

    @Column(name = "associated_order")
    private String associatedOrder;
}
