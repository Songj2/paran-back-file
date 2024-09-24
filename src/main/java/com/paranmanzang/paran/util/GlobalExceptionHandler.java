package com.paranmanzang.paran.util;

import com.amazonaws.SdkClientException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.paranmanzang.paran.model.domain.ErrorField;
import com.paranmanzang.paran.model.domain.ExceptionResponseModel;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<?> cloudException(SdkClientException e){
        List<ErrorField> errors= new ArrayList<>();
        errors.add(new ErrorField("", e.getMessage().substring(0, e.getMessage().indexOf("("))));
        return ResponseEntity.badRequest().body(
                new ExceptionResponseModel("CE", "SdkClientException", errors)
        );
    }

}