package com.mecong.restservice.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EmailData {
    int batch;
    String email;
    int count;
}
