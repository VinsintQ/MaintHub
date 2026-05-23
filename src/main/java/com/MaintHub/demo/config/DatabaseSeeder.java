package com.MaintHub.demo.config;

import com.MaintHub.demo.enums.EquipmentStatus;
import com.MaintHub.demo.model.Equipment;
import com.MaintHub.demo.model.EquipmentCategory;
import com.MaintHub.demo.model.Role;
import com.MaintHub.demo.model.RoleName;
import com.MaintHub.demo.model.User;
import com.MaintHub.demo.repository.EquipmentCategoryRepository;
import com.MaintHub.demo.repository.EquipmentRepository;
import com.MaintHub.demo.repository.RoleRepository;
import com.MaintHub.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
@Order(2)
public class DatabaseSeeder implements CommandLineRunner {
    private static final String DEFAULT_PASSWORD = "Password123!";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EquipmentCategoryRepository categoryRepository;
    private final EquipmentRepository equipmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            EquipmentCategoryRepository categoryRepository,
            EquipmentRepository equipmentRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.equipmentRepository = equipmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedEquipmentCategories();
        seedEquipment();
    }

    private void seedUsers() {
        createUserIfMissing("System Admin", "admin@mainthub.com", "admin@repairflow.com", RoleName.ROLE_ADMIN);
        createUserIfMissing("Staff User", "staff@mainthub.com", "staff@repairflow.com", RoleName.ROLE_STAFF);
        createUserIfMissing("Technician User", "technician@mainthub.com", "technician@repairflow.com", RoleName.ROLE_TECHNICIAN);
        createUserIfMissing("Inspector User", "inspector@mainthub.com", "inspector@repairflow.com", RoleName.ROLE_INSPECTOR);
    }

    private void createUserIfMissing(String userName, String email, String legacyEmail, RoleName roleName) {
        if (userRepository.existsByEmailAddress(email)) {
            return;
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Missing seed role: " + roleName.name()));

        User legacyUser = userRepository.findUserByEmailAddress(legacyEmail);
        if (legacyUser != null) {
            legacyUser.setUserName(userName);
            legacyUser.setEmailAddress(email);
            legacyUser.getRoles().add(role);
            userRepository.save(legacyUser);
            return;
        }

        User user = new User();
        user.setUserName(userName);
        user.setEmailAddress(email);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setAccountVerified(true);
        user.setActive(true);
        user.getRoles().add(role);

        userRepository.save(user);
    }

    private void seedEquipmentCategories() {
        createCategoryIfMissing("Heavy Equipment", "Cranes, forklifts, generators, and large workshop assets", true, 90);
        createCategoryIfMissing("Safety Equipment", "Fire extinguishers, harnesses, and safety-critical equipment", true, 30);
        createCategoryIfMissing("IT Equipment", "Computers, printers, scanners, and network devices", false, null);
    }

    private void createCategoryIfMissing(
            String name,
            String description,
            boolean requiresRegularInspection,
            Integer inspectionIntervalDays
    ) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            return;
        }

        EquipmentCategory category = new EquipmentCategory();
        category.setName(name);
        category.setDescription(description);
        category.setRequiresRegularInspection(requiresRegularInspection);
        category.setInspectionIntervalDays(inspectionIntervalDays);

        categoryRepository.save(category);
    }

    private void seedEquipment() {
        EquipmentCategory heavyEquipment = findCategory("Heavy Equipment");
        EquipmentCategory safetyEquipment = findCategory("Safety Equipment");
        EquipmentCategory itEquipment = findCategory("IT Equipment");

        Arrays.asList(
                createEquipment("Forklift 01", "FL-1001", "Warehouse forklift", "Warehouse A", 8, heavyEquipment),
                createEquipment("Generator 01", "GN-2001", "Backup power generator", "Plant Room", 7, heavyEquipment),
                createEquipment("Fire Extinguisher 01", "FE-3001", "CO2 fire extinguisher", "Main Entrance", 10, safetyEquipment),
                createEquipment("Printer 01", "PR-4001", "Office network printer", "Admin Office", 9, itEquipment)
        ).forEach(this::saveEquipmentIfMissing);
    }

    private Equipment createEquipment(
            String name,
            String serialNumber,
            String description,
            String location,
            Integer conditionLevel,
            EquipmentCategory category
    ) {
        Equipment equipment = new Equipment();
        equipment.setName(name);
        equipment.setSerialNumber(serialNumber);
        equipment.setDescription(description);
        equipment.setLocation(location);
        equipment.setConditionLevel(conditionLevel);
        equipment.setStatus(EquipmentStatus.ACTIVE);
        equipment.setPurchaseDate(LocalDate.now().minusYears(1));
        equipment.setWarrantyExpiryDate(LocalDate.now().plusYears(1));
        equipment.setCategory(category);
        return equipment;
    }

    private void saveEquipmentIfMissing(Equipment equipment) {
        if (!equipmentRepository.existsBySerialNumberIgnoreCase(equipment.getSerialNumber())) {
            equipmentRepository.save(equipment);
        }
    }

    private EquipmentCategory findCategory(String name) {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing seed category: " + name));
    }
}
