// API - defines the endpoints starting with /patients

package com.michellely.hospital_api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

    @PostMapping
    public String createPatient(@RequestBody Map<String, Object> body) {
        try {
            int rowsAffected = jdbcTemplate.update(
                    "INSERT INTO patients (age, time_in_hospital, n_procedures, n_lab_procedures, " +
                            "n_medications, n_outpatient, n_inpatient, n_emergency, medical_specialty, " +
                            "diag_1, diag_2, diag_3, glucose_test, a1c_test, change, diabetes_med, readmitted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    body.get("age"),
                    body.get("time_in_hospital"),
                    body.get("n_procedures"),
                    body.get("n_lab_procedures"),
                    body.get("n_medications"),
                    body.get("n_outpatient"),
                    body.get("n_inpatient"),
                    body.get("n_emergency"),
                    body.get("medical_specialty"),
                    body.get("diag_1"),
                    body.get("diag_2"),
                    body.get("diag_3"),
                    body.get("glucose_test"),
                    body.get("a1c_test"),
                    body.get("change"),
                    body.get("diabetes_med"),
                    body.get("readmitted")
            );
            if (rowsAffected == 1) {
                return "Patient created successfully!";
            } else {
                return ("Something went wrong!");
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

    }

    @PutMapping("/{id}")
    public String updatePatient(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            int rowsAffected = jdbcTemplate.update(
                    "UPDATE patients SET age = ?, time_in_hospital = ?, n_procedures = ?, " +
                            "n_lab_procedures = ?, n_medications = ?, n_outpatient = ?, n_inpatient = ?, " +
                            "n_emergency = ?, medical_specialty = ?, diag_1 = ?, diag_2 = ?, diag_3 = ?, " +
                            "glucose_test = ?, a1c_test = ?, change = ?, diabetes_med = ?, readmitted = ? " +
                            "WHERE id = ?",
                    body.get("age"),
                    body.get("time_in_hospital"),
                    body.get("n_procedures"),
                    body.get("n_lab_procedures"),
                    body.get("n_medications"),
                    body.get("n_outpatient"),
                    body.get("n_inpatient"),
                    body.get("n_emergency"),
                    body.get("medical_specialty"),
                    body.get("diag_1"),
                    body.get("diag_2"),
                    body.get("diag_3"),
                    body.get("glucose_test"),
                    body.get("a1c_test"),
                    body.get("change"),
                    body.get("diabetes_med"),
                    body.get("readmitted"),
                    id
            );
            if (rowsAffected == 1) {
                return ("Patient " + id + " successfully updated!");
            } else {
                return "Something went wrong!";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @PatchMapping("/{id}")
    public String patchPatient(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            StringBuilder sb = new StringBuilder("SET ");
            List<Object> values = new ArrayList<>();
            for (String key : body.keySet()) {
                sb.append(key + " = ?,");
                values.add(body.get(key));
            }

            String setClause = sb.toString();
            setClause = setClause.substring(0, setClause.length() - 1);
            values.add(id);
            int rowsAffected = jdbcTemplate.update("UPDATE patients " + setClause + " WHERE id = ?", values.toArray());
            if (rowsAffected == 1) {
                return ("Patient " + id + " successfully updated!");
            } else {
                return ("Something went wrong!");
            }
        } catch (Exception e) {
            return "Error :" + e.getMessage();
        }
    }
}