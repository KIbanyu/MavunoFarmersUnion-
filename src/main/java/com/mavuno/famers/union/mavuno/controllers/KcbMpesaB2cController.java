package com.mavuno.famers.union.mavuno.controllers;


import com.mavuno.famers.union.mavuno.models.ApiMpesaB2cReq;
import com.mavuno.famers.union.mavuno.models.ResponseModel;
import com.mavuno.famers.union.mavuno.services.MpesaKcbDisburseService;
import org.codehaus.jackson.JsonProcessingException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/kcb/mpesa")
public class KcbMpesaB2cController {

    @Autowired
    private MpesaKcbDisburseService mpesaKcbDisburseService;

    @PostMapping(value = "/b2c/request")
    public ResponseModel mpesaB2cRequest() throws com.fasterxml.jackson.core.JsonProcessingException {

        JSONObject jsonObject = new JSONObject(jsonObjectFromKCB());
        JSONObject header = jsonObject.getJSONObject("header");
        ApiMpesaB2cReq apiMpesaB2cReq = new ApiMpesaB2cReq();

        if (header.getString("serviceName").equalsIgnoreCase("FundsTransfer")) {
            JSONObject requestPayload = jsonObject.getJSONObject("requestPayload");
            JSONObject transactionInfo = requestPayload.getJSONObject("transactionInfo");

            String mobilePhone = transactionInfo.getString("credintMobileNumber");
            String amount = transactionInfo.getString("transactionAmount");

            if (!mobilePhone.equals("")) {

                apiMpesaB2cReq.setPhoneNumber(mobilePhone);
                apiMpesaB2cReq.setAmount(new BigDecimal(Integer.parseInt(amount)));

                mpesaKcbDisburseService.mpesaRequestB2cTransaction(apiMpesaB2cReq);
            }


            System.out.println(transactionInfo);
        }



        return mpesaKcbDisburseService.mpesaRequestB2cTransaction(apiMpesaB2cReq);

    }


    public String jsonObjectFromKCB() {

        String jsonFromCoreBanking = "{\n" +
                "    \"header\": {\n" +
                "        \"messageID\": \"12345666\",\n" +
                "        \"featureCode\": \"201\",\n" +
                "        \"featureName\": \"FinancialTransactions\",\n" +
                "        \"serviceCode\": \"2001\",\n" +
                "        \"serviceName\": \"FundsTransfer\",\n" +
                "        \"serviceSubCategory\":\"Account\",\n" +
                "        \"minorServiceVersion\": \"1.0\",\n" +
                "        \"channelCode\": \"01\",\n" +
                "        \"channelName\": \"atm\",\n" +
                "        \"routeCode\": \"01\",  \n" +
                "        \"timeStamp\": \"22222\",\n" +
                "        \"serviceMode\": \"sync\",  \n" +
                "        \"subscribeEvents\": \"1\",\n" +
                "        \"callBackURL\": \"\"  \n" +
                "    },\n" +
                "   \"requestPayload\": {\n" +
                "     \n" +
                "      \"transactionInfo\": {\n" +
                "         \"companyCode\": \"KE0010001\",\n" +
                "         \"transactionType\": \"Payment Notification\",\n" +
                "         \"creditAccountNumber\": \"\",\n" +
                "\"credintMobileNumber\":\"0723995657\",\n" +
                "         \"transactionAmount\": \"200\",\n" +
                "         \"transactionReference\": \"\",\n" +
                "         \"currencyCode\": \"ke\",\n" +
                "          \"amountCurrency\": \"KES\",\n" +
                "          \"dateTime\": \"\",\n" +
                "          \"dateString\": \"\"\n" +
                "      }\n" +
                "   }\n" +
                "}";


        return jsonFromCoreBanking;
    }



}