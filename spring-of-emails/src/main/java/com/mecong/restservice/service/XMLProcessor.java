package com.mecong.restservice.service;

import com.mecong.restservice.model.XMLProcessorSummary;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class XMLProcessor {
    EmailProcessorService emailProcessorService;
    UrlProcessorService urlProcessorService;

    public XMLProcessorSummary processXMLInput(InputStream inputStream) throws XMLStreamException {
        int emailsProcessed = 0;
        int invalidEmails = 0;
        int urlsDiscovered = 0;

        try (StaxStreamProcessor processor = new StaxStreamProcessor(inputStream)) {
            XMLStreamReader reader = processor.getReader();

            while (reader.hasNext()) {       // while not end of XML
                int event = reader.next();   // read next event
                if (event == START_ELEMENT && "email".equals(reader.getLocalName())) {
                    String email = reader.getElementText();
                    if (emailProcessorService.processEmail(email)) {
                        emailsProcessed++;
                    } else {
                        invalidEmails++;
                    }
                } else if (event == START_ELEMENT && "url".equals(reader.getLocalName())) {
                    String url = reader.getElementText();
                    urlProcessorService.processUrl(url);
                    urlsDiscovered++;
                }
            }
        }

        return XMLProcessorSummary.builder()
                .emailsProcessed(emailsProcessed)
                .urlsDiscovered(urlsDiscovered)
                .invalidEmails(invalidEmails)
                .build();
    }
}
