package com.dembasiby.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Column(nullable = false)
    private String street;
    private String street2;

    @Column(nullable = false)
    private String city;
    private String state;
    private String zip;
    @Column(nullable = false)
    private String country;
}
