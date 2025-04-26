package com.dembasiby.user.entity;

import jakarta.persistence.*;

@Entity
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String street;
    private String street2;

    @Column(nullable = false)
    private String city;
    private String state;
    private String zip;
    @Column(nullable = false)
    private String country;
    @ManyToOne(fetch = FetchType.LAZY)
    private UserProfile userProfile;
}
