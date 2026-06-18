package com.example.i_commerce.domain.member.repository;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByEmailHash(String emailHash);

    boolean existsByEmailHash(String emailHash);

    boolean existsByAdminRole(AdminRole role);

    Page<Admin> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Admin> findByIdAndDeletedAtIsNull(Long id);

    long countByAdminRoleAndAdminStatusAndDeletedAtIsNull(
        AdminRole adminRole,
        AdminStatus adminStatus
    );

    List<Admin> findAllByEmailHashIn(Collection<String> emailHashes);
}
