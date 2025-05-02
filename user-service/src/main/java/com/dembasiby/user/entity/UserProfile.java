package com.dembasiby.user.entity;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @ElementCollection
    private Set<Address> addresses;

    public UserProfile(String firstName, String lastName, Set<Address> addresses) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.addresses = addresses;
    }

    public UserProfile() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }
}
