// API - defines the endpoints starting with /patients

package com.michellely.hospital_api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController

// all endpoints start with /patients
@RequestMapping("/patients")
public class PatientController {

    private final JdbcTemplate jdbcTemplate;

    public PatientController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 100 patients
    @GetMapping
    public List<Map<String, Object>> getAllPatients() {
        return jdbcTemplate.queryForList("SELECT * FROM patients LIMIT 100");
    }

    // one specific patient by id
    @GetMapping("/{id}")
    public Map<String, Object> getPatientById(@PathVariable int id) {
        return jdbcTemplate.queryForMap("SELECT * FROM patients WHERE id = ?", id);
    }

    // readmitted patients
    @GetMapping("/readmitted")
    public List<Map<String, Object>> getReadmittedPatients() {
        return jdbcTemplate.queryForList("SELECT * FROM patients WHERE readmitted = 'yes' LIMIT 100");
    }

    // filter by diagnosis
    @GetMapping("/diagnosis/{diag}")
    public List<Map<String, Object>> getPatientsByDiagnosis(@PathVariable String diag){
        return jdbcTemplate.queryForList("SELECT * FROM patients WHERE diag_1 = ? OR diag_2 = ? OR diag_3 = ? LIMIT 100", diag, diag, diag);
    }

    // aggregated statistics
    @GetMapping("/stats")
    public Map<String, Object> getPatientStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        Long totalPatients = jdbcTemplate.queryForObject("SELECT COUNT(*) AS total_patients FROM patients", Long.class);
        Double averageHospitalTime = jdbcTemplate.queryForObject("SELECT AVG(time_in_hospital) AS average_time_in_hospital FROM patients", Double.class);
        Long totalReadmitted = jdbcTemplate.queryForObject("SELECT COUNT(*) AS total_readmitted FROM patients WHERE readmitted = 'yes'", Long.class);
        String mostCommonDiag = jdbcTemplate.queryForObject("SELECT diag_1 FROM patients GROUP BY diag_1 ORDER BY COUNT(*) DESC LIMIT 1", String.class);

        stats.put("total_patients", totalPatients);
        stats.put("average_time_in_hospital", averageHospitalTime);
        stats.put("total_readmitted", totalReadmitted);
        stats.put("most_common_diagnosis", mostCommonDiag);
        return stats;
    }
}