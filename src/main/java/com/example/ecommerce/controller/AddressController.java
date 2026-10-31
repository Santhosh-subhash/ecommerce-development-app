
package com.example.ecommerce.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecommerce.entity.Address;
import com.example.ecommerce.repository.AddressRepository;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressRepository addressRepository;

    public AddressController(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    private Long getLoggedInUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in before saving your address"
            );
        }

        return Long.valueOf(userId.toString());
    }

    @PostMapping
    public ResponseEntity<Address> saveAddress(
            @RequestBody Address address,
            HttpSession session) {

        Long userId = getLoggedInUserId(session);

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
                    "Please provide all required address fields"
            );
        }

        if (address.getAddressLine1().trim().length() < 5 || address.getAddressLine1().length() > 200
                || (address.getAddressLine2() != null && address.getAddressLine2().length() > 200)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address line must contain 5 to 200 characters");
        }
        if (address.getCity().trim().length() > 100 || address.getState().trim().length() > 100
                || address.getCountry().trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City, state and country must not exceed 100 characters");
        }
        if (!address.getPostalCode().trim().matches("[0-9]{4,10}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Postal code must contain 4 to 10 digits");
        }

        address.setAddressLine1(address.getAddressLine1().trim());
        address.setCity(address.getCity().trim());
        address.setState(address.getState().trim());
        address.setPostalCode(address.getPostalCode().trim());
        address.setCountry(address.getCountry().trim());
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

        Address savedAddress = addressRepository.save(address);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedAddress);
    }

    @GetMapping
    public ResponseEntity<List<Address>> getAddresses(HttpSession session) {

        Long userId = getLoggedInUserId(session);

        return ResponseEntity.ok(
                addressRepository.findByUserId(userId)
        );
    }
}
