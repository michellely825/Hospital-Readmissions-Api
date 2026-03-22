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

    // GET all patients (paginated)
    @GetMapping
    public List<Map<String, Object>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return jdbcTemplate.queryForList("SELECT * FROM patients ORDER BY id ASC LIMIT ? OFFSET ?", size, page * size);
    }

    // GET one specific patient by id
    @GetMapping("/{id}")
    public Map<String, Object> getPatientById(@PathVariable int id) {
        return jdbcTemplate.queryForMap("SELECT * FROM patients WHERE id = ?", id);
    }

    // GET all readmitted patients (paginated)
    @GetMapping("/readmitted")
    public List<Map<String, Object>> getReadmittedPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return jdbcTemplate.queryForList("SELECT * FROM patients WHERE readmitted = 'yes' LIMIT ? OFFSET ?", size, page * size);
    }

    // GET all patients by diagnosis (paginated)
    @GetMapping("/diagnosis/{diag}")
    public List<Map<String, Object>> getPatientsByDiagnosis(
            @PathVariable String diag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return jdbcTemplate.queryForList("SELECT * FROM patients WHERE diag_1 = ? OR diag_2 = ? OR diag_3 = ? LIMIT ? OFFSET ?", diag, diag, diag, size, page * size);
    }

    // Get aggregated statistics
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

    // Readmission rates by primary diagnosis
    @GetMapping("/stats/by-diagnosis")
    public List<Map<String, Object>> getStatsByDiag() {
        return jdbcTemplate.queryForList("SELECT diag_1, COUNT(*) AS total, SUM(CASE WHEN readmitted = 'yes' THEN 1 ELSE 0 END) AS total_readmitted, ROUND(SUM(CASE WHEN readmitted = 'yes' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS readmission_rates  FROM patients GROUP BY diag_1 ORDER BY diag_1 ASC");
    }

    // Readmission rates by age
    @GetMapping("stats/by-age")
    public List<Map<String, Object>> getStatsByAge() {
        return jdbcTemplate.queryForList("SELECT age, COUNT(*) AS total, SUM(CASE WHEN readmitted = 'yes' THEN 1 ELSE 0 END) AS total_readmitted, ROUND(SUM(CASE WHEN readmitted = 'yes' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS readmission_rates  FROM patients GROUP BY age ORDER BY age ASC");
    }

    // Create a new patient
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

    // Replace a patient by id
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

    // Partially update a patient by id
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
            return "Error: " + e.getMessage();
        }
    }

    // Delete a patient
    @DeleteMapping("/{id}")
    public String deletePatient(@PathVariable int id) {
        try {
            int affectedRows = jdbcTemplate.update("DELETE FROM patients WHERE id = ?", id);
            if (affectedRows == 1) {
                return "Patient " + id + " successfully deleted!";
            } else {
                return "Something went wrong!";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


}