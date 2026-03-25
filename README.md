# Hospital Readmissions API

## Overview

A Spring Boot REST API serving ten years of hospital readmission data for diabetic patients across
130 US hospitals (1999-2008). Deployed on Railway with a live public URL.

## Live Deployment

This API is deployed on **Railway** with a managed **PostgreSQL** database.

🌐 LIVE Base URL: `https://hospital-readmissions-api-production.up.railway.app`

Try it (open in new tab):

- [GET /patients](https://hospital-readmissions-api-production.up.railway.app/patients)
- [GET /patients/stats](https://hospital-readmissions-api-production.up.railway.app/patients/stats)
- [GET /patients/stats/by-diagnosis](https://hospital-readmissions-api-production.up.railway.app/patients/stats/by-diagnosis)
- [GET /patients/stats/by-age](https://hospital-readmissions-api-production.up.railway.app/patients/stats/by-age)
- [GET /patients/25](https://hospital-readmissions-api-produßction.up.railway.app/patients/25)
- [GET /patients/readmitted](https://hospital-readmissions-api-production.up.railway.app/patients/readmitted)
- [GET /patients/diagnosis/Diabetes](https://hospital-readmissions-api-production.up.railway.app/patients/diagnosis/Diabetes)

## Features

- 25,000 rows of real healthcare data
- Full CRUD endpoints (Create, Read, Update, Delete)
- Raw SQL with JdbcTemplate
- Pagination support
- Filtered queries by diagnosis and readmission status
- Readmission rate analytics by diagnosis and age
- Aggregated statistics endpoint
- Deployed on Railway with a managed PostgreSQL database

## Tech-Stack

- **Backend:** Java, Spring Boot
- **Database:** PostgreSQL
- **Deployment:** Railway
- **API Testing:** Postman

## Endpoints

### GET ALL Patients

`GET /patients`

Returns all patients from the database. Supports pagination.

**Query Parameters:**
| Parameter | Default | Description |
|---|---|---|
| page | 0 | Page number |
| size | 20 | Number of results per page |

**Example:**

```bash
curl "http://localhost:8080/patients?page=0&size=20"
```

**Response:**

```json
[
  {
    "id": 1,
    "age": "[70-80)",
    "time_in_hospital": 8,
    "readmitted": "no"
  }
]
```

---

### GET Patient By ID

`GET /patients/{id}`

Returns one specific patient by id.

**Example:**

```bash
curl http://localhost:8080/patients/1
```

**Response:**

```json
{
  "id": 1,
  "age": "[70-80)",
  "time_in_hospital": 8,
  "readmitted": "no"
}
```

---

### GET Readmitted Patients

`GET /patients/readmitted`

Returns all readmitted patients. Supports pagination.

**Query Parameters:**
| Parameter | Default | Description |
|---|---|---|
| page | 0 | Page number |
| size | 20 | Number of results per page |

**Example:**

```bash
curl "http://localhost:8080/patients/readmitted?page=0&size=20"
```

**Response:**

```json
[
  {
    "id": 3,
    "age": "[50-60)",
    "time_in_hospital": 5,
    "readmitted": "yes"
  }
]
```

---

### GET Patients By Diagnosis

`GET /patients/diagnosis/{diag}`

Returns all patients where any diagnosis (primary, secondary, or tertiary) matches the specified diagnosis. Supports pagination.

**Query Parameters:**
| Parameter | Default | Description |
|---|---|---|
| page | 0 | Page number |
| size | 20 | Number of results per page |

**Example:**

```bash
curl "http://localhost:8080/patients/diagnosis/Diabetes?page=0&size=20"
```

**Response:**

```json
[
  {
    "id": 1,
    "age": "[70-80)",
    "time_in_hospital": 8,
    "diag_1": "Diabetes",
    "readmitted": "yes"
  }
]
```

---

### GET Patients Statistics

`GET /patients/stats`

Returns aggregated patient statistics.
**Example:**

```bash
curl http://localhost:8080/patients/stats
```

**Response:**

```json
{
  "total_patients": 25000,
  "average_time_in_hospital": 4.45332,
  "total_readmitted": 11754,
  "most_common_diagnosis": "Circulatory"
}
```

---

### GET Stats By Diagnosis

`GET /patients/stats/by-diagnosis`

Returns readmission rates grouped by primary diagnosis.

**Example:**

```bash
curl http://localhost:8080/patients/stats/by-diagnosis
```

**Response:**

```json
[
  {
    "diag_1": "Diabetes",
    "total": 1747,
    "total_readmitted": 937,
    "readmission_rates": 53.63
  }
]
```

---

### GET Stats By Age

`GET /patients/stats/by-age`

Returns readmission rates grouped by age bracket.

**Example:**

```bash
curl http://localhost:8080/patients/stats/by-age
```

**Response:**

```json
[
  {
    "age": "[70-80)",
    "total": 6836,
    "total_readmitted": 3336,
    "readmission_rates": 48.8
  }
]
```

---

### Create Patient

`POST /patients`

Creates a new patient record.

**Example:**

```bash
curl -X POST http://localhost:8080/patients \
  -H "Content-Type: application/json" \
  -d '{"age": "[20-30)", "time_in_hospital": 3, "n_procedures": 10, "n_lab_procedures": 2, "n_medications": 5, "n_outpatient": 0, "n_inpatient": 0, "n_emergency": 0, "medical_specialty": "Other", "diag_1": "Circulatory", "diag_2": "Other", "diag_3": "Other", "glucose_test": "no", "a1c_test": "no", "change": "no", "diabetes_med": "yes", "readmitted": "no"}'
```

**Response:**

```
Patient created successfully!
```

---

### Replace Patient

`PUT /patients/{id}`

Replaces all fields of an existing patient by id.

**Example:**

```bash
curl -X PUT http://localhost:8080/patients/1 \
  -H "Content-Type: application/json" \
  -d '{"age": "[30-40)", "time_in_hospital": 5, "n_procedures": 10, "n_lab_procedures": 2, "n_medications": 5, "n_outpatient": 0, "n_inpatient": 0, "n_emergency": 0, "medical_specialty": "Other", "diag_1": "Circulatory", "diag_2": "Other", "diag_3": "Other", "glucose_test": "no", "a1c_test": "no", "change": "no", "diabetes_med": "yes", "readmitted": "no"}'
```

**Response:**

```
Patient 1 successfully updated!
```

---

### Update Patient

`PATCH /patients/{id}`

Updates only the specified fields of an existing patient by id.

**Example:**

```bash
curl -X PATCH http://localhost:8080/patients/1 \
  -H "Content-Type: application/json" \
  -d '{"age": "[80-90)"}'
```

**Response:**

```
Patient 1 successfully updated!
```

---

### Delete Patient

`DELETE /patients/{id}`

Deletes a patient by id.
**Example:**

```bash
curl -X DELETE http://localhost:8080/patients/1
```

**Response:**

```
Patient 1 successfully deleted!
```

## Postman Collection

A Postman collection is included in the repository (`Hospital Readmissions API.postman_collection.json`) with all 11 endpoints pre-configured.

Import it into Postman and set the `base_url` environment variable to either:

- Local: `http://localhost:8080`
- Production: `https://hospital-readmissions-api-production.up.railway.app`

## Dataset

- **Source:** [Kaggle - Hospital Readmissions](https://www.kaggle.com/datasets/dubradave/hospital-readmissions/data)
- **Records:** 25,000 patient encounters
- **Time Period:** 1999-2008
- **Features:** age, time in hospital, diagnoses, medications, readmission status and more

## How to Run Locally

### Prerequisites

- Java 21+
- PostgreSQL 16
- Maven (included via Maven Wrapper)

### Setup

1. **Clone the repository**

```bash
   git clone https://github.com/michellely/hospital-api.git
   cd hospital-api
```

2. **Set up PostgreSQL**

```bash
   brew install postgresql@16
   brew services start postgresql@16
```

3. **Create the database and table**

```bash
   psql postgres
```

```sql
   CREATE DATABASE hospital;
   \c hospital
   CREATE TABLE patients (
       id SERIAL PRIMARY KEY,
       age VARCHAR(20),
       time_in_hospital INT,
       n_procedures INT,
       n_lab_procedures INT,
       n_medications INT,
       n_outpatient INT,
       n_inpatient INT,
       n_emergency INT,
       medical_specialty VARCHAR(100),
       diag_1 VARCHAR(100),
       diag_2 VARCHAR(100),
       diag_3 VARCHAR(100),
       glucose_test VARCHAR(20),
       a1c_test VARCHAR(20),
       change VARCHAR(10),
       diabetes_med VARCHAR(10),
       readmitted VARCHAR(10)
   );
```

4. **Load the dataset**

```bash
   psql -d hospital -c "\COPY patients(age,
    time_in_hospital, n_procedures, n_lab_procedures, n_medications,
    n_outpatient, n_inpatient,
    n_emergency, medical_specialty,
    diag_1, diag_2, diag_3,
    glucose_test, a1c_test,
    change, diabetes_med,
    readmitted)
    FROM
    '/your/path/to/hospital_readmissions.csv'
    DELIMITER ',' CSV HEADER;"
```

5. **Configure database connection**

   Open `src/main/resources/application.properties` and update:

```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/hospital
   spring.datasource.username=your_mac_username
   spring.datasource.password=
```

6. **Run the server**

```bash
   ./mvnw spring-boot:run
```

7. **Test it**

```bash
   curl http://localhost:8080/patients
```
