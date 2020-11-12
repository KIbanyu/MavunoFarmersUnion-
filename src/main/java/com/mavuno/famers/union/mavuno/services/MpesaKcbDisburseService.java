package com.mavuno.famers.union.mavuno.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mavuno.famers.union.mavuno.models.*;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Transactional
@Service
public class MpesaKcbDisburseService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${mpesa.base.url}")
    private String MPESA_BASE_URL;
    @Value("${mpesa.consumer.key}")
    private String MPESA_CONSUMER_KEY;
    @Value("${mpesa.secret.key}")
    private String MPESA_SECRET_KEY;
    @Value("${mpesa.shortcode}")
    private String MPESA_SHORT_CODE;
    @Value("${mpesa.api.user.name}")
    private String MPESA_API_USERNAME;
    @Value("${mpesa.api.password}")
    private String MPESA_API_PASSWORD;

    public static String encryptInitiatorPassword(String password) {

        Logger logger = LoggerFactory.getLogger(MpesaKcbDisburseService.class);
        String encryptedPassword = null;

        try {

            Security.addProvider(new BouncyCastleProvider());
            byte[] input = password.getBytes();

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
            File file = new File("config/mpesa.cer");
            FileInputStream fin = new FileInputStream(file);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(fin);
            PublicKey pk = certificate.getPublicKey();
            cipher.init(Cipher.ENCRYPT_MODE, pk);

            byte[] cipherText = cipher.doFinal(input);

            // Convert the resulting encrypted byte array into a string using base64 encoding
            encryptedPassword = Base64.getEncoder().encodeToString(cipherText); //Base64.getEncoder().encodeToString(cipherText);

        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | FileNotFoundException | CertificateException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            logger.info("[ ENCRYPTION ERROR ]", ex);
        }

        return encryptedPassword;
    }

    public ResponseModel mpesaRequestB2cTransaction(ApiMpesaB2cReq req) throws JsonProcessingException {

        ApiToken apiToken = getMpesaToken(MPESA_CONSUMER_KEY, MPESA_SECRET_KEY);
        String shortcode = MPESA_SHORT_CODE;

        String msisdn = req.getPhoneNumber();
        String url = MPESA_BASE_URL + "/mpesa/b2c/v1/paymentrequest";

        MpesaB2CReq mpesaB2CReq = new MpesaB2CReq();
        mpesaB2CReq.setAmount(req.getAmount().toString());
        mpesaB2CReq.setCommandID("PromotionPayment");
        mpesaB2CReq.setInitiatorName(MPESA_API_USERNAME.trim());
        mpesaB2CReq.setOccasion("payment");
        mpesaB2CReq.setPartyA(shortcode);
        mpesaB2CReq.setPartyB(msisdn);
        mpesaB2CReq.setSecurityCredential(encryptInitiatorPassword(MPESA_API_PASSWORD.trim()));
        mpesaB2CReq.setRemarks("payment");
        mpesaB2CReq.setResultURL("https://task.kcb.co.ke/api/v1/m/callback/b2c");
        mpesaB2CReq.setQueueTimeOutURL("https://task.kcb.co.ke//api/v1/m/callback/b2c");

        //generate trans ref
        String paymentRef = "MP-" + System.currentTimeMillis();


        ResponseModel responseModel = new ResponseModel();
        Map<String, Object> map = new HashMap<>();

        String json = prepareRequestJson(mpesaB2CReq);
        logger.info("==================  json body : " + json);
        MpesaTransferResponse mpesaTransferResponse = apiMpesaB2CHttpRequest(apiToken.getAccess_token(), json, url);
        logger.info("************** response to the other party {} ->  " + new ObjectMapper().writeValueAsString(mpesaTransferResponse));

        if (null != mpesaTransferResponse.getConversationID()) {
            if (mpesaTransferResponse.getResponseCode().equalsIgnoreCase("0")) {
                responseModel.setMessage(mpesaTransferResponse.getResponseDescription());
                responseModel.setStatus("00");
                map.put("conversionId", mpesaTransferResponse.getConversationID());
                map.put("originConversionId", mpesaTransferResponse.getOriginatorConversationID());
                map.put("description", mpesaTransferResponse.getResponseDescription());
                responseModel.setData(map);
            } else {
                responseModel.setMessage(mpesaTransferResponse.getResponseDescription());
                responseModel.setStatus("00");
                map.put("conversionId", mpesaTransferResponse.getConversationID());
                map.put("originConversionId", mpesaTransferResponse.getOriginatorConversationID());
                map.put("description", mpesaTransferResponse.getResponseDescription());
                responseModel.setData(map);


            }
        } else {
            map.put("errorCode", mpesaTransferResponse.getErrorCode());
            map.put("requestId", mpesaTransferResponse.getRequestId());
            map.put("errorMessage", mpesaTransferResponse.getErrorMessage());
            responseModel.setData(map);
            responseModel.setMessage(mpesaTransferResponse.getErrorMessage());
            responseModel.setStatus("01");


        }


        return responseModel;
    }

    public ApiToken getMpesaToken(String consumerKey, String secretKey) {
        ApiToken apiToken = null;
        Gson gson = new Gson();
        Response response;

        String appKeySecret = consumerKey + ":" + secretKey;

        String url = "https://sandbox.safaricom.co.ke" + "/oauth/v1/generate?grant_type=client_credentials";
        logger.info("*********** get token url "+url);
        //String auth = Base64.encode(bytes);
        String auth = Base64.getEncoder().encodeToString(appKeySecret.getBytes());
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("authorization", "Basic " + auth)
                .addHeader("cache-control", "no-cache")
                .build();
        try {
            response = client.newCall(request).execute();

            if (response.code() == 200) {
                assert response.body() != null;
                apiToken = gson.fromJson(response.body().string(), ApiToken.class);
                logger.info("Access token given " + apiToken.getAccess_token());
                return apiToken;
            } else if (response.code() == 401) {
                logger.error("Could not get access token. Access denied.Invalid credentials : {}", response.body().string());
            } else {
                logger.error("Error occurred fetching api access token : {} ", response.body().string());
            }
            logger.info("\n" + "token generation  " + response.body().string() + " \n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiToken;
    }

    public String prepareRequestJson(Object request) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(request);

    }

    public MpesaTransferResponse apiMpesaB2CHttpRequest(String token, String json, String url) {
        MpesaTransferResponse response;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", "Bearer " + token);

        try {
            ResponseEntity<MpesaTransferResponse> mpesaB2CResponseResponseEntity = sendAPIRequest(url, json, httpHeaders, MpesaTransferResponse.class);

            response = mpesaB2CResponseResponseEntity.getBody();
            assert response != null;
            logger.info("================== api url response : " + response.toString());

            return response;
        } catch (HttpStatusCodeException e) {
            e.printStackTrace();
            //response.setErrorMessage(e.getMessage());
            logger.info("********** http request to " + url + "  exception occurred " + e.getMessage());
            logger.info("********** http response error body " + e.getResponseBodyAsString());
            response = errorHandler(e.getResponseBodyAsString());

        }
        return response;
    }

    public MpesaTransferResponse errorHandler(String message) {
        MpesaTransferResponse response = new MpesaTransferResponse();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(message);
            response = objectMapper.readValue(jsonObj.toString(), MpesaTransferResponse.class);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return response;

    }

    public ResponseEntity<MpesaTransferResponse> sendAPIRequest(String switchUrl, String json, HttpHeaders httpHeaders, Class<MpesaTransferResponse> clazz) {
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<Object> httpEntity = new HttpEntity<>(json, httpHeaders);

        UriComponents uriComponents = UriComponentsBuilder.fromUriString(switchUrl)
                .build()
                .encode();

        return restTemplate.exchange(uriComponents.toString(), HttpMethod.POST, httpEntity, clazz);
    }
}