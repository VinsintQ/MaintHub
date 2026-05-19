package com.MaintHub.demo.repository;



import com.MaintHub.demo.model.Role;
import com.MaintHub.demo.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    boolean existsByName(RoleName name);
    Optional<Role> findByName(RoleName name);
}
