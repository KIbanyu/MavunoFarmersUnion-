package com.mavuno.famers.union.mavuno.models;

import com.sun.istack.NotNull;

import java.math.BigDecimal;

public class ApiMpesaB2cReq {
    @NotNull
    private String phoneNumber;
    @NotNull
    private BigDecimal amount;
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


}
