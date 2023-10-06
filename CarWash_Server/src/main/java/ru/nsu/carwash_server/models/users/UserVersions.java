package ru.nsu.carwash_server.models.users;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.nsu.carwash_server.payload.request.UpdateUserInfoRequest;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "user_version")
@NoArgsConstructor
@ToString
@Getter
@Setter
@AllArgsConstructor
public class UserVersions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @NotNull
    @JsonIgnore
    private User user;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    @Column(name = "creation_time")
    private Date dateOfCreation;

    @Column(name = "phone")
    private String phone;

    @Email
    @Column(name = "email")
    private String email;

    @Column(name = "bonuses")
    private int bonuses;

    @NotBlank
    @ToString.Exclude
    @JsonIgnore
    @Column(name = "password")
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "admin_note")
    private String adminNote;

    @Column(name = "comments")
    private String comments;

    @Column(name = "version")
    private int version;

    public UserVersions(UserVersions userVersions, UpdateUserInfoRequest updateUserInfoRequest) {
        this.user = userVersions.getUser();

        this.version = userVersions.getVersion() + 1;

        this.dateOfCreation = new Date();

        this.password = userVersions.getPassword();

        this.bonuses = userVersions.getBonuses();

        this.phone = (updateUserInfoRequest.getPhone() != null) ?
                updateUserInfoRequest.getPhone() : userVersions.getPhone();

        this.fullName = (updateUserInfoRequest.getFullName() != null) ?
                updateUserInfoRequest.getFullName() : userVersions.getFullName();

        this.adminNote = (updateUserInfoRequest.getAdminNote() != null) ?
                updateUserInfoRequest.getAdminNote() : userVersions.getAdminNote();

        this.comments = (updateUserInfoRequest.getUserNote() != null) ?
                updateUserInfoRequest.getUserNote() : userVersions.getComments();

        this.email = (updateUserInfoRequest.getEmail() != null) ?
                updateUserInfoRequest.getEmail() : userVersions.getEmail();
    }

    public UserVersions(UserVersions userVersions, String newPassword, String newUsername) {
        this.user = userVersions.getUser();

        this.version = userVersions.getVersion() + 1;

        this.dateOfCreation = new Date();

        this.password = (newPassword != null) ?
                newPassword : userVersions.getPassword();

        this.bonuses = userVersions.getBonuses();

        this.phone = (newUsername != null) ?
                newUsername : userVersions.getPhone();

        this.fullName = userVersions.getFullName();

        this.adminNote = userVersions.getAdminNote();

        this.comments = userVersions.getComments();

        this.email = userVersions.getEmail();
    }
}
