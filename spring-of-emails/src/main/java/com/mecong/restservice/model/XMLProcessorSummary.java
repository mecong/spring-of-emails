package com.mecong.restservice.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class XMLProcessorSummary {
    int emailsProcessed;
    int invalidEmails;
    int urlsDiscovered;
}
