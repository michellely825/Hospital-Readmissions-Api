package com.michellely.hospital_api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class WelcomeController {

    // root endpoint
    @GetMapping("/")
    public Map<String, Object> welcome() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "Hospital Readmissions API");
        info.put("version", "1.0");
        info.put("github", "https://github.com/michellely/hospital-api");
        info.put("endpoints", new String[]{
                "GET /patients",
                "GET /patients/{id}",
                "GET /patients/readmitted",
                "GET /patients/diagnosis/{diag}",
                "GET /patients/stats",
                "GET /patients/stats/by-diagnosis",
                "GET /patients/stats/by-age",
                "POST /patients",
                "PUT /patients/{id}",
                "PATCH /patients/{id}",
                "DELETE /patients/{id}"
        });
        return info;
    }

}
