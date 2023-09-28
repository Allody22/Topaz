package ru.nsu.carwash_server.models.operations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "operations")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Operations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "operations", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    private List<OperationsVersions> operationsVersions = new ArrayList<>();
}
