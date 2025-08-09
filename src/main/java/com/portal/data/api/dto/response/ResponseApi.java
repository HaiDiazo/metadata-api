package com.portal.data.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseApi<T> {
    private int statusCode;
    private double timeExecution;
    private String statusMessage;
    private T data;

    public ResponseApi(int statusCode, double timeExecution, String statusMessage, T data) {
        this.statusCode = statusCode;
        this.timeExecution = timeExecution;
        this.statusMessage = statusMessage;
        this.data = data;
    }
}

