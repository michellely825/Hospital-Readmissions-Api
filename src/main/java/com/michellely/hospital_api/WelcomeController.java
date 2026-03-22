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
        info.put("endpoints", "/patients, /patients/stats, /patients/stats/by-diagnosis, /patients/stats/by-age");
        info.put("github", "https://github.com/michellely/hospital-api");
        return info;
    }

}
