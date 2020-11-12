package com.mavuno.famers.union.mavuno.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "OriginatorConversationID",
        "ConversationID",
        "ResponseCode",
        "ResponseDescription"
})
public class MpesaTransferResponse {
    @JsonProperty("OriginatorConversationID")
    public String originatorConversationID;
    @JsonProperty("ConversationID")
    public String conversationID;
    @JsonProperty("ResponseCode")
    public String responseCode;
    @JsonProperty("ResponseDescription")
    public String responseDescription;

    //error response
    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("requestId")
    private String requestId;

    public String getOriginatorConversationID() {
        return originatorConversationID;
    }

    public void setOriginatorConversationID(String originatorConversationID) {
        this.originatorConversationID = originatorConversationID;
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseDescription() {
        return responseDescription;
    }

    public void setResponseDescription(String responseDescription) {
        this.responseDescription = responseDescription;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "MpesaB2CResponse \n{" +
                "\noriginatorConversationID='" + originatorConversationID + '\'' +
                ", \n conversationID='" + conversationID + '\'' +
                ",  \nresponseCode='" + responseCode + '\'' +
                ", \n responseDescription='" + responseDescription + '\'' +
                ", \n errorMessage='" + errorMessage + '\'' +
                ",\n errorCode='" + errorCode + '\'' +
                ", \n requestId='" + requestId + '\'' +
                "\n"+'}';
    }
}
