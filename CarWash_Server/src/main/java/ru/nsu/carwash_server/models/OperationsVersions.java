package ru.nsu.carwash_server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "operations_versions")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class OperationsVersions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "operations_id")
    @NotNull
    @JsonIgnore
    private Operations operations;

    @Column(name = "creation_time")
    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date dateOfCreation;

    @Column(name = "version")
    private int version;

    @Column(name = "changes")
    private String changes;

    @OneToMany(mappedBy = "operation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OperationsUserLink> operationUserLinks = new ArrayList<>();
}
