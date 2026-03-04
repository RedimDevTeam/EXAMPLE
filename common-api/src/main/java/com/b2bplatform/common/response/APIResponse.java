package com.b2bplatform.common.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.b2bplatform.common.enums.StatusCode;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class APIResponse {

    private StatusCode status;
    private Object result;

    public int getCode() {
        return status != null ? status.value() : 0;
    }

    public static APIResponse get(StatusCode code) {
        APIResponse response = new APIResponse();
        response.status = code;
        return response;
    }

    public static APIResponse get(StatusCode code, Object object) {
        APIResponse response = new APIResponse();
        response.status = code;
        response.result = object;
        return response;
    }

    public static APIResponse get(String code, Object object) {
        return APIResponse.get(StatusCode.valueOf(code), object);
    }

    public static APIResponse success(Object result) {
        APIResponse response = new APIResponse();
        response.status = StatusCode.SUCCESS;
        response.result = result;
        return response;
    }
}
