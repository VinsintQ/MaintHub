package com.MaintHub.demo.dto;


import lombok.Getter;

@Getter
public class ChangePasswordRequest {

    private String newPass;
    private String oldPass;
}
