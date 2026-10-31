package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name="ADDRESSES")
public class Address {

    @Id
    @SequenceGenerator(name="addressSeq", sequenceName="ADDRESSES_SEQ", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="addressSeq")
    @Column(name="ADDRESS_ID", nullable=false)
    private Long addressId;

    @Column(name="USER_ID", nullable=true)
    private Long userId;

    @Column(name="ADDRESS_LINE1", nullable=true, length=200)
    private String addressLine1;

    @Column(name="ADDRESS_LINE2", nullable=false, length=200)
    private String addressLine2;

    @Column(name="CITY", nullable=true, length=100)
    private String city;

    @Column(name="STATE", nullable=true, length=100)
    private String state;

    @Column(name="POSTAL_CODE", nullable=true, length=15)
    private String postalCode;

    @Column(name="COUNTRY", nullable=true, length=100)
    private String country;

    @Column(name="ADDRESS_TYPE", nullable=true, length=20)
    private String addressType;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name="IS_DEFAULT", nullable=true, length=1)
    private String isDefault;

    @Column(name="CREATED_AT", nullable=true, insertable=false, updatable=false)
    private LocalDateTime createdAt;

    public Address() {}

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAddressType() { return addressType; }
    public void setAddressType(String addressType) { this.addressType = addressType; }

    public String getIsDefault() { return isDefault; }
    public void setIsDefault(String isDefault) { this.isDefault = isDefault; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}