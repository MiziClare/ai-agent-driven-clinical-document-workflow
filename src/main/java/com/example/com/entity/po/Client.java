package com.example.com.entity.po;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

// Client entity
@Data
public class Client implements Serializable {
    private Integer clientId;
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String gender;
    private String healthCardNum;
    private String phone;
    private String email;
    private String address;
    private String postalCode;
    private String emergencyContact;
    private String notes;
}
