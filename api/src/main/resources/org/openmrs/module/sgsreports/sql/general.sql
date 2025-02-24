WITH PatientBase AS (
    SELECT DISTINCT
        p.person_id,
        p.gender,
        p.birthdate,
        TIMESTAMPDIFF(YEAR, p.birthdate, CURDATE()) AS age,
        pn.given_name,
        pn.family_name
    FROM person p
    INNER JOIN patient pt ON p.person_id = pt.patient_id
    LEFT JOIN person_name pn ON p.person_id = pn.person_id 
        AND pn.voided = 0
    WHERE p.voided = 0
),
PersonAttributes AS (
    SELECT 
        pa.person_id,
        pat.name AS attribute_name,
        MAX(pa.value) AS attribute_value
    FROM person_attribute pa
    JOIN person_attribute_type pat ON pa.person_attribute_type_id = pat.person_attribute_type_id
    WHERE pa.voided = 0
    GROUP BY pa.person_id, pat.name
),
LatestObservations AS (
    SELECT 
        o.person_id,
        cn.name AS concept_name,
        o.obs_datetime,
        o.value_numeric,
        o.value_text,
        GROUP_CONCAT(
            DISTINCT COALESCE(answer_cn.name, o.value_text)
            ORDER BY COALESCE(answer_cn.name, o.value_text)
            SEPARATOR ', '
        ) AS grouped_values,
        o.creator AS creator_id
    FROM obs o
    JOIN concept_name cn ON o.concept_id = cn.concept_id 
        AND cn.locale = 'en' 
        AND cn.concept_name_type = 'FULLY_SPECIFIED'
        AND cn.voided = 0
    LEFT JOIN concept_name answer_cn ON o.value_coded = answer_cn.concept_id 
        AND answer_cn.locale = 'en' 
        AND answer_cn.concept_name_type = 'FULLY_SPECIFIED'
        AND answer_cn.voided = 0
    WHERE o.voided = 0
    AND cn.name IN ('Weight', 'Anticipated Procedure', 'Equipment Needed', 'Operative length (min)')
    AND DATE(o.obs_datetime) = :obsDate
    GROUP BY 
        o.person_id,
        cn.name,
        o.obs_datetime,
        o.value_numeric,
        o.value_text,
        o.creator
),
LatestDiagnoses AS (
    SELECT
        ed.patient_id,
        COALESCE(
            diagnosis_cn.name,
            ed.diagnosis_non_coded,
            coded_name_cn.name
        ) AS diagnosis,
        ed.certainty,
        ed.dx_rank,
        ed.date_created,
        ROW_NUMBER() OVER (
            PARTITION BY ed.patient_id
            ORDER BY ed.date_created DESC
        ) AS diagnosis_rank
    FROM encounter_diagnosis ed
    LEFT JOIN concept_name diagnosis_cn 
        ON ed.diagnosis_coded = diagnosis_cn.concept_id
        AND diagnosis_cn.locale = 'en'
        AND diagnosis_cn.concept_name_type = 'FULLY_SPECIFIED'
        AND diagnosis_cn.voided = 0
    LEFT JOIN concept_name coded_name_cn 
        ON ed.diagnosis_coded_name = coded_name_cn.concept_id
        AND coded_name_cn.locale = 'en'
        AND coded_name_cn.concept_name_type = 'FULLY_SPECIFIED'
        AND coded_name_cn.voided = 0
    WHERE ed.voided = 0
    AND DATE(ed.date_created) = :obsDate
),
UserInfo AS (
    SELECT 
        u.user_id,
        pn.given_name,
        pn.family_name,
        CONCAT(pn.given_name, ' ', pn.family_name) AS full_name
    FROM users u
    JOIN person p ON u.person_id = p.person_id
    JOIN person_name pn ON p.person_id = pn.person_id
    WHERE u.retired = 0
    AND pn.voided = 0
),
ObservationSurgeons AS (
    SELECT
        lo.person_id,
        GROUP_CONCAT(
            DISTINCT ui.full_name
            ORDER BY ui.full_name
            SEPARATOR ', '
        ) AS all_surgeons
    FROM LatestObservations lo
    JOIN UserInfo ui ON lo.creator_id = ui.user_id
    GROUP BY lo.person_id
)
SELECT 
    pb.person_id,
    pb.gender,
    pb.birthdate,
    pb.age,
    pb.given_name,
    pb.family_name,
    MAX(CASE WHEN pa.attribute_name = 'Race' THEN pa.attribute_value END) as race,
    MAX(CASE WHEN pa.attribute_name = 'Citizenship' THEN pa.attribute_value END) as citizenship,
    MAX(CASE WHEN pa.attribute_name = 'Telephone Number' THEN pa.attribute_value END) as phone_number,
    MAX(CASE WHEN lo.concept_name = 'Weight' THEN lo.value_numeric END) as weight,
    MAX(CASE WHEN lo.concept_name = 'Anticipated Procedure' THEN 
        COALESCE(lo.value_text, lo.grouped_values)
    END) as anticipated_procedure,
    MAX(CASE WHEN lo.concept_name = 'Equipment Needed' THEN lo.grouped_values END) as equipment_needed,
    MAX(CASE WHEN lo.concept_name = 'Operative length (min)' THEN lo.value_numeric END) as operative_length_min,
    MAX(CASE WHEN ld.diagnosis_rank = 1 THEN ld.diagnosis END) as primary_diagnosis,
    MAX(CASE WHEN ld.diagnosis_rank = 1 THEN ld.certainty END) as diagnosis_certainty,
    MAX(CASE WHEN ld.diagnosis_rank = 1 THEN ld.dx_rank END) as diagnosis_rank,
    GROUP_CONCAT(
        DISTINCT CASE WHEN ld.diagnosis_rank > 1 THEN ld.diagnosis END
        ORDER BY ld.diagnosis_rank
        SEPARATOR ', '
    ) as secondary_diagnoses,
    os.all_surgeons as surgeons,
    MAX(CASE WHEN lo.concept_name = 'Weight' THEN 
        CONCAT(ui.full_name, ' (Weight)')
    END) as weight_surgeon,
    MAX(CASE WHEN lo.concept_name = 'Anticipated Procedure' THEN 
        CONCAT(ui.full_name, ' (Procedure)')
    END) as procedure_surgeon,
    MAX(CASE WHEN lo.concept_name = 'Equipment Needed' THEN 
        CONCAT(ui.full_name, ' (Equipment)')
    END) as equipment_surgeon,
    MAX(CASE WHEN lo.concept_name = 'Operative length (min)' THEN 
        CONCAT(ui.full_name, ' (Op Length)')
    END) as oplength_surgeon
FROM PatientBase pb
LEFT JOIN PersonAttributes pa ON pb.person_id = pa.person_id
LEFT JOIN LatestObservations lo ON pb.person_id = lo.person_id
LEFT JOIN LatestDiagnoses ld ON pb.person_id = ld.patient_id
LEFT JOIN UserInfo ui ON lo.creator_id = ui.user_id
LEFT JOIN ObservationSurgeons os ON pb.person_id = os.person_id
WHERE (lo.person_id IS NOT NULL OR ld.patient_id IS NOT NULL)  -- Ensures only patients with obs/diagnosis on the given date
GROUP BY 
    pb.person_id,
    pb.gender,
    pb.birthdate,
    pb.age,
    pb.given_name,
    pb.family_name,
    os.all_surgeons
ORDER BY pb.person_id;
