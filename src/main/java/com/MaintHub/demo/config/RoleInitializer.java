package com.MaintHub.demo.config;

import com.MaintHub.demo.model.Role;
import com.MaintHub.demo.model.RoleName;
import com.MaintHub.demo.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@Order(1)
public class RoleInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final DataSource dataSource;

    public RoleInitializer(RoleRepository roleRepository, DataSource dataSource) {
        this.roleRepository = roleRepository;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        dropLegacyRoleNameCheckConstraint();
        createRoleIfMissing(RoleName.ROLE_ADMIN);
        createRoleIfMissing(RoleName.ROLE_STAFF);
        createRoleIfMissing(RoleName.ROLE_TECHNICIAN);
        createRoleIfMissing(RoleName.ROLE_INSPECTOR);
        createRoleIfMissing(RoleName.ROLE_USER);
    }

    private void createRoleIfMissing(RoleName roleName) {
        if (!roleRepository.existsByName(roleName)) {
            roleRepository.save(new Role(null, roleName));
        }
    }

    private void dropLegacyRoleNameCheckConstraint() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_name_check");
        } catch (SQLException ignored) {
            // Some databases do not create enum check constraints for varchar-backed enums.
        }
    }
}
