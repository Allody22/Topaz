package ru.nsu.carwash_server.models.orders;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.nsu.carwash_server.models.secondary.constants.OrderTypes;
import ru.nsu.carwash_server.payload.request.UpdateOrderInfoRequest;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "orders_versions")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class OrderVersions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    @NotNull
    @JsonIgnore
    private Order order;

    @Column(name = "creation_time")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date dateOfCreation;

    @Column(name = "start_time")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date startTime;

    @Column(name = "end_time")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date endTime;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "orders_washing_link",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "washing_id"))
    private List<OrdersWashing> ordersWashing = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "orders_polishing_link",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "polishing_id"))
    private List<OrdersPolishing> ordersPolishings = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "orders_tire_link",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "tire_id"))
    private List<OrdersTire> ordersTires = new ArrayList<>();

    @Column(name = "administrator")
    private String administrator;

    @Column(name = "specialist")
    private String specialist;

    @Column(name = "auto_number")
    private String autoNumber;

    @Column(name = "auto_type")
    private int autoType;

    @Column(name = "box_number")
    private int boxNumber;

    @Column(name = "sale")
    private String sale;

    @Column(name = "bonuses")
    private int bonuses;

    @Column(name = "price")
    private int price;

    @Column(name = "wheel_radius")
    private String wheelR;

    @Column(name = "comments")
    private String comments;

    @Column(name = "user_contacts")
    private String userContacts;

    @Column(name = "order_type")
    private String orderType;

    @Column(name = "current_status")
    private String currentStatus;

    @Column(name = "version")
    private int version;

    public OrderVersions(OrderVersions oldOrderVersion, UpdateOrderInfoRequest newOrderVersion, List<OrdersTire> ordersTires,
                         List<OrdersPolishing> ordersPolishings, List<OrdersWashing> ordersWashings,
                         List<OrdersTire> actualOrdersTires, List<OrdersPolishing> actualOrdersPolishings,
                         List<OrdersWashing> actualOrdersWashings) {
        this.order = oldOrderVersion.getOrder();

        this.version = oldOrderVersion.getVersion() + 1;

        this.dateOfCreation = new Date();

        this.startTime = (newOrderVersion.getStartTime() != null) ?
                newOrderVersion.getStartTime() : oldOrderVersion.getStartTime();

        this.sale = (newOrderVersion.getSale() != null) ?
                newOrderVersion.getSale() : oldOrderVersion.getSale();


        this.endTime = (newOrderVersion.getEndTime() != null) ?
                newOrderVersion.getEndTime() : oldOrderVersion.getEndTime();

        this.ordersWashing = (!ordersWashings.isEmpty()) ?
                ordersWashings : actualOrdersWashings;

        this.ordersPolishings = (!ordersPolishings.isEmpty()) ?
                ordersPolishings : actualOrdersPolishings;

        this.ordersTires = (!ordersTires.isEmpty()) ?
                ordersTires : actualOrdersTires;

        this.administrator = (newOrderVersion.getAdministrator() != null) ?
                newOrderVersion.getAdministrator() : oldOrderVersion.getAdministrator();

        this.specialist = (newOrderVersion.getSpecialist() != null) ?
                newOrderVersion.getSpecialist() : oldOrderVersion.getSpecialist();

        this.autoNumber = (newOrderVersion.getAutoNumber() != null) ?
                newOrderVersion.getAutoNumber() : oldOrderVersion.getAutoNumber();

        this.autoType = (newOrderVersion.getAutoType() != null) ?
                newOrderVersion.getAutoType() : oldOrderVersion.getAutoType();

        this.boxNumber = (newOrderVersion.getBoxNumber() != null) ?
                newOrderVersion.getBoxNumber() : oldOrderVersion.getBoxNumber();

        this.bonuses = (newOrderVersion.getBonuses() != null) ?
                newOrderVersion.getBonuses() : oldOrderVersion.getBonuses();

        this.price = (newOrderVersion.getPrice() != null) ?
                newOrderVersion.getPrice() : oldOrderVersion.getPrice();

        this.wheelR = (newOrderVersion.getWheelR() != null) ?
                newOrderVersion.getWheelR() : oldOrderVersion.getWheelR();

        this.comments = (newOrderVersion.getComments() != null) ?
                newOrderVersion.getComments() : oldOrderVersion.getComments();

        this.userContacts = (newOrderVersion.getUserPhone() != null) ?
                newOrderVersion.getUserPhone() : oldOrderVersion.getUserContacts();

        this.orderType = (newOrderVersion.getOrderType() != null) ?
                newOrderVersion.getOrderType() : oldOrderVersion.getOrderType();

        this.currentStatus = (newOrderVersion.getCurrentStatus() != null) ?
                newOrderVersion.getCurrentStatus() : oldOrderVersion.getCurrentStatus();

        this.version = oldOrderVersion.getVersion() + 1;
    }

    public OrderVersions(List<OrdersWashing> washingOrders, Date startTime,
                         Date endTime, Date currentTime, String administrator,
                         String specialist, int boxNumber, int bonuses,
                         String comments, String autoNumber,
                         int autoType, String userContacts, String orderType,
                         String currentStatus, int price, int version, String sale) {
        this.ordersWashing = washingOrders;
        this.price = price;
        this.sale = sale;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dateOfCreation = currentTime;
        this.administrator = administrator;
        this.specialist = specialist;
        this.boxNumber = boxNumber;
        this.bonuses = bonuses;
        this.comments = comments;
        this.autoNumber = autoNumber;
        this.autoType = autoType;
        this.userContacts = userContacts;
        this.orderType = orderType;
        this.wheelR = OrderTypes.washOrder;
        this.currentStatus = currentStatus;
        this.version = version;
    }

    public OrderVersions(List<OrdersPolishing> ordersPolishings, Date startTime,
                         Date endTime, Date currentTime, String administrator,
                         String specialist, int boxNumber, int bonuses,
                         String comments, String autoNumber,
                         int autoType, String userContacts, String orderType,
                         int price, String currentStatus, int version, String sale) {
        this.ordersPolishings = ordersPolishings;
        this.startTime = startTime;
        this.sale = sale;
        this.endTime = endTime;
        this.dateOfCreation = currentTime;
        this.administrator = administrator;
        this.specialist = specialist;
        this.boxNumber = boxNumber;
        this.bonuses = bonuses;
        this.comments = comments;
        this.autoNumber = autoNumber;
        this.autoType = autoType;
        this.userContacts = userContacts;
        this.orderType = orderType;
        this.wheelR = OrderTypes.polishing;
        this.price = price;
        this.currentStatus = currentStatus;
        this.version = version;
    }

    public OrderVersions(List<OrdersTire> ordersTires, Date startTime, Date endTime,
                         Date currentTime, String administrator, String specialist, int boxNumber,
                         int bonuses, String comments, String autoNumber,
                         int autoType, String userContacts, String orderType,
                         int price, String wheelR, String currentStatus,
                         int version, String sale) {
        this.ordersTires = ordersTires;
        this.sale = sale;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dateOfCreation = currentTime;
        this.administrator = administrator;
        this.specialist = specialist;
        this.boxNumber = boxNumber;
        this.bonuses = bonuses;
        this.comments = comments;
        this.autoNumber = autoNumber;
        this.autoType = autoType;
        this.userContacts = userContacts;
        this.orderType = orderType;
        this.price = price;
        this.wheelR = wheelR;
        this.currentStatus = currentStatus;
        this.version = version;
    }

    public OrderVersions(OrderVersions oldOrderVersion, OrderVersions newOrderVersion) {
        this.order = oldOrderVersion.getOrder();

        this.version = oldOrderVersion.getVersion() + 1;

        this.dateOfCreation = new Date();

        this.sale = (newOrderVersion.getSale() != null) ?
                newOrderVersion.getSale() : oldOrderVersion.getSale();

        this.startTime = (newOrderVersion.getStartTime() != null) ?
                newOrderVersion.getStartTime() : oldOrderVersion.getStartTime();


        this.endTime = (newOrderVersion.getEndTime() != null) ?
                newOrderVersion.getEndTime() : oldOrderVersion.getEndTime();

        this.ordersWashing = (newOrderVersion.getOrdersWashing() != null) ?
                newOrderVersion.getOrdersWashing() : oldOrderVersion.getOrdersWashing();

        this.ordersPolishings = (newOrderVersion.getOrdersPolishings() != null) ?
                newOrderVersion.getOrdersPolishings() : oldOrderVersion.getOrdersPolishings();

        this.ordersTires = (newOrderVersion.getOrdersTires() != null) ?
                newOrderVersion.getOrdersTires() : oldOrderVersion.getOrdersTires();

        this.administrator = (newOrderVersion.getAdministrator() != null) ?
                newOrderVersion.getAdministrator() : oldOrderVersion.getAdministrator();

        this.specialist = (newOrderVersion.getSpecialist() != null) ?
                newOrderVersion.getSpecialist() : oldOrderVersion.getSpecialist();

        this.autoNumber = (newOrderVersion.getAutoNumber() != null) ?
                newOrderVersion.getAutoNumber() : oldOrderVersion.getAutoNumber();

        this.autoType = (newOrderVersion.getAutoType() != 0) ?
                newOrderVersion.getAutoType() : oldOrderVersion.getAutoType();

        this.boxNumber = (newOrderVersion.getBoxNumber() != 0) ?
                newOrderVersion.getBoxNumber() : oldOrderVersion.getBoxNumber();

        this.bonuses = (newOrderVersion.getBonuses() != 0) ?
                newOrderVersion.getBonuses() : oldOrderVersion.getBonuses();

        this.price = (newOrderVersion.getPrice() != 0) ?
                newOrderVersion.getPrice() : oldOrderVersion.getPrice();

        this.wheelR = (newOrderVersion.getWheelR() != null) ?
                newOrderVersion.getWheelR() : oldOrderVersion.getWheelR();

        this.comments = (newOrderVersion.getComments() != null) ?
                newOrderVersion.getComments() : oldOrderVersion.getComments();

        this.userContacts = (newOrderVersion.getUserContacts() != null) ?
                newOrderVersion.getUserContacts() : oldOrderVersion.getUserContacts();

        this.orderType = (newOrderVersion.getOrderType() != null) ?
                newOrderVersion.getOrderType() : oldOrderVersion.getOrderType();

        this.currentStatus = (newOrderVersion.getCurrentStatus() != null) ?
                newOrderVersion.getCurrentStatus() : oldOrderVersion.getCurrentStatus();

        this.version = oldOrderVersion.getVersion() + 1;
    }
}
