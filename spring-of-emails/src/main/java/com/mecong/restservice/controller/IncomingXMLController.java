package com.mecong.restservice.controller;

import com.mecong.restservice.model.XMLProcessorSummary;
import com.mecong.restservice.service.XMLProcessor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;


@Controller
@RequestMapping("/feed")
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class IncomingXMLController {
    XMLProcessor xmlProcessor;

    @PostMapping
    @ResponseBody
    public XMLProcessorSummary postFeed(HttpServletRequest request) throws XMLStreamException, IOException {
        return xmlProcessor.processXMLInput(request.getInputStream());
    }
}
