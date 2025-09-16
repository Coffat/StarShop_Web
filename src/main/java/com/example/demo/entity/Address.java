package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {

    @Column(nullable = false)
    private String street;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructors
    public Address() {
    }

    public Address(String street, String city, String province, Boolean isDefault, User user) {
        this.street = street;
        this.city = city;
        this.province = province;
        this.isDefault = isDefault;
        this.user = user;
    }
    
    // Helper methods
    public String getFullAddress() {
        return street + ", " + city + ", " + province;
    }

    // Getters and Setters
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
