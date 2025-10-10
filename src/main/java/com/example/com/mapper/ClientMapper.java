package com.example.com.mapper;

import com.example.com.entity.po.Client;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClientMapper {
    @Select("SELECT * FROM course_ehealth_client WHERE client_id = #{clientId}")
    Client selectById(@Param("clientId") Integer clientId);

    @Select("SELECT * FROM course_ehealth_Client")
    List<Client> selectAll();

    @Insert("INSERT INTO course_ehealth_client (first_name, last_name, date_of_birth, gender, health_card_num, phone, email, address, postal_code, emergency_contact, notes) " +
            "VALUES (#{firstName}, #{lastName}, #{dateOfBirth}, #{gender}, #{healthCardNum}, #{phone}, #{email}, #{address}, #{postalCode}, #{emergencyContact}, #{notes})")
    @Options(useGeneratedKeys = true, keyProperty = "clientId")
    int insert(Client client);

    @Update("UPDATE course_ehealth_client SET first_name=#{firstName}, last_name=#{lastName}, date_of_birth=#{dateOfBirth}, gender=#{gender}, health_card_num=#{healthCardNum}, phone=#{phone}, email=#{email}, address=#{address}, postal_code=#{postalCode}, emergency_contact=#{emergencyContact}, notes=#{notes} WHERE client_id=#{clientId}")
    int update(Client client);

    @Delete("DELETE FROM course_ehealth_client WHERE client_id=#{clientId}")
    int delete(@Param("clientId") Integer clientId);
}
