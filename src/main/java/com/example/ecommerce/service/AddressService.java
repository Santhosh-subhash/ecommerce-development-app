
package com.example.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecommerce.entity.Address;
import com.example.ecommerce.repository.AddressRepository;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional
    public Address saveAddress(Long userId, Address address) {

        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Please log in first");
        }

        if (address.getAddressLine1() == null
                || address.getAddressLine1().isBlank()
                || address.getCity() == null
                || address.getCity().isBlank()
                || address.getState() == null
                || address.getState().isBlank()
                || address.getPostalCode() == null
                || address.getPostalCode().isBlank()
                || address.getCountry() == null
                || address.getCountry().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please provide all required address fields");
        }

        address.setAddressId(null);
        address.setUserId(userId);

        if (address.getAddressLine2() == null) {
            address.setAddressLine2("");
        }

        if (address.getAddressType() == null
                || address.getAddressType().isBlank()) {
            address.setAddressType("HOME");
        }

        if (address.getIsDefault() == null
                || address.getIsDefault().isBlank()) {
            address.setIsDefault("Y");
        }

        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public Object getAddresses(Long userId) {

        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Please log in first");
        }

        return addressRepository.findByUserId(userId);
    }
}
