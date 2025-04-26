package com.dembasiby.user.entity;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private User user;
    private String firstName;
    private String lastName;
    @OneToMany
    private Set<Address> addresses;
}
