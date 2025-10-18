package com.example.mapper;

import com.example.entity.po.Prescription;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PrescriptionMapper {
    @Select("SELECT * FROM course_ehealth_Prescription WHERE prescription_id = #{prescriptionId}")
    Prescription selectById(@Param("prescriptionId") String prescriptionId);

    @Select("SELECT * FROM course_ehealth_Prescription WHERE client_id = #{clientId}")
    List<Prescription> selectByClientId(@Param("clientId") Integer clientId);

    @Select("SELECT * FROM course_ehealth_Prescription WHERE client_id = #{clientId} ORDER BY date_prescribed DESC LIMIT 1")
    Prescription selectLatestByClientId(@Param("clientId") Integer clientId);

    @Insert("INSERT INTO course_ehealth_Prescription " +
            "(prescription_id, client_id, prescriber_id, medication_name, medication_strength, medication_form, dosage_instructions, quantity, refills_allowed, date_prescribed, expiry_date, pharmacy_name, pharmacy_address, status, notes) " +
            "VALUES (#{prescriptionId}, #{clientId}, #{prescriberId}, #{medicationName}, #{medicationStrength}, #{medicationForm}, #{dosageInstructions}, #{quantity}, #{refillsAllowed}, #{datePrescribed}, #{expiryDate}, #{pharmacyName}, #{pharmacyAddress}, #{status}, #{notes})")
    int insert(Prescription prescription);

    @Update("UPDATE course_ehealth_Prescription SET " +
            "client_id=#{clientId}, prescriber_id=#{prescriberId}, medication_name=#{medicationName}, medication_strength=#{medicationStrength}, medication_form=#{medicationForm}, dosage_instructions=#{dosageInstructions}, quantity=#{quantity}, refills_allowed=#{refillsAllowed}, date_prescribed=#{datePrescribed}, expiry_date=#{expiryDate}, pharmacy_name=#{pharmacyName}, pharmacy_address=#{pharmacyAddress}, status=#{status}, notes=#{notes} " +
            "WHERE prescription_id=#{prescriptionId}")
    int update(Prescription prescription);

    @Delete("DELETE FROM course_ehealth_Prescription WHERE prescription_id=#{prescriptionId}")
    int delete(@Param("prescriptionId") String prescriptionId);
}
