package com.example.com.mapper;

import com.example.com.entity.po.Requisition;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface RequisitionMapper {
    @Select("SELECT * FROM course_ehealth_Requisition WHERE requisition_id = #{requisitionId}")
    Requisition selectById(@Param("requisitionId") String requisitionId);

    @Select("SELECT * FROM course_ehealth_Requisition WHERE client_id = #{clientId}")
    List<Requisition> selectByClientId(@Param("clientId") Integer clientId);

    @Insert("INSERT INTO course_ehealth_Requisition " +
            "(requisition_id, client_id, requester_id, department, test_type, test_code, clinical_info, date_requested, priority, status, lab_name, lab_address, result_date, notes) " +
            "VALUES (#{requisitionId}, #{clientId}, #{requesterId}, #{department}, #{testType}, #{testCode}, #{clinicalInfo}, #{dateRequested}, #{priority}, #{status}, #{labName}, #{labAddress}, #{resultDate}, #{notes})")
    int insert(Requisition requisition);

    @Update("UPDATE course_ehealth_Requisition SET " +
            "client_id=#{clientId}, requester_id=#{requesterId}, department=#{department}, test_type=#{testType}, test_code=#{testCode}, clinical_info=#{clinicalInfo}, date_requested=#{dateRequested}, priority=#{priority}, status=#{status}, lab_name=#{labName}, lab_address=#{labAddress}, result_date=#{resultDate}, notes=#{notes} " +
            "WHERE requisition_id=#{requisitionId}")
    int update(Requisition requisition);

    @Delete("DELETE FROM course_ehealth_Requisition WHERE requisition_id=#{requisitionId}")
    int delete(@Param("requisitionId") String requisitionId);

    @Select("SELECT * FROM course_ehealth_Requisition WHERE client_id = #{clientId} ORDER BY date_requested DESC LIMIT 1")
    Requisition selectLatestByClientId(@Param("clientId") Integer clientId);
}
