package com.example.i_commerce.domain.member.entity;


import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String emailHash;

    @Column(nullable = false)
    private byte[] emailEncrypted;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private byte[] name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole adminRole;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminStatus adminStatus = AdminStatus.ACTIVE;

//    @Builder.Default
//    @OneToMany(mappedBy = "admin")
//    private List<AdminLoginHistory> loginHistories = new ArrayList<>();

    public void changeRole(AdminRole adminRole) {
        this.adminRole = adminRole;
    }

    public void changeStatus(AdminStatus adminStatus) {
        this.adminStatus = adminStatus;
    }

    public boolean isActiveMaster() {
        return this.adminRole == AdminRole.MASTER
            && this.adminStatus == AdminStatus.ACTIVE
            && this.getDeletedAt() == null;
    }

    public void lock() {
        if (this.adminStatus != AdminStatus.ACTIVE) {
            return;
        }

        this.adminStatus = AdminStatus.LOCKED;
    }
}
