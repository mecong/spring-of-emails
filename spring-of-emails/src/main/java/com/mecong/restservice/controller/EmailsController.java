package com.mecong.restservice.controller;

import com.mecong.restservice.service.EmailsService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/emails")
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EmailsController {
    EmailsService emailsService;

    @GetMapping
    public Map<String, Map<Integer, Integer>> getEmailsCount() {
        return emailsService.getEmailsCounts();
    }

    @GetMapping("/{email}")
    public Map<String, Map<Integer, Integer>> getEmailsCount(@PathVariable String email) {
        return emailsService.getEmailCounts(email);
    }

}
