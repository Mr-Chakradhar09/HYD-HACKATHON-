package com.inventory.authservice.entity;

import com.inventory.authservice.enums.RoleType;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, unique = true)
    private RoleType roleName;
    @ManyToMany(mappedBy = "roles")
    private Set<Employee> employees = new HashSet<>();
    public Role() {
    }
    public Role(Long id, RoleType roleName, Set<Employee> employees) {
        this.id = id;
        this.roleName = roleName;
        this.employees = employees;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public RoleType getRoleName() {
        return roleName;
    }
    public void setRoleName(RoleType roleName) {
        this.roleName = roleName;
    }
    public Set<Employee> getEmployees() {
        return employees;
    }
    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }
}