package com.example.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocTools {

//    @Tool
//    public String helloTool(String name) {
//        return "Hello, " + name + "!";
//    }
//
//    @Tool(description = "查询城市北京明天的天气")
//    public String getBeijingWeather(@ToolParam(description = "要查询的城市名", required = false) String city) {
//        return city + " 明天多云转晴，最高气温 27°C";
//    }
//
//    @Tool(description = "查询城市合肥明天的天气")
//    public String getHeifeiWeather(@ToolParam(description = "要查询的城市名", required = false) String city) {
//        return city + " 明天下雨，最高气温 21°C";
//    }
//
//    // 根据给出的用户ID查出指定用户的基本信息表的全部内容
//    @Tool(description = "根据给出的用户ID查出指定用户的基本信息表的全部内容")
//    public String getUserInfo(@ToolParam(description = "用户ID", required = true) int clientId) {
//        // Here you would add code to fetch user info from a database
//        return "用户ID: " + clientId + ", 姓名: 张三, 年龄: 30, 性别: 男, 诊断: 感冒";
//    }
//
//    // 生成 prescription 和 requisition
//    @Tool(description = "根据指定用户的基本信息表随机生成一张的prescription和一张requisition")
//    public String generateDocuments(@ToolParam(description = "用户ID", required = true) int clientId) {
//        // Here you would add code to generate the documents based on userInfo
//        return "已为用户生成了新的prescription和requisition。";
//    }
//
//    // 根据指定用户的家庭地址查出离其最近的三个的pharmacy和三个lab机构
//    @Tool(description = "根据指定用户的家庭地址查出离其最近的三个的pharmacy和三个lab机构")
//    public String findNearbyFacilities(@ToolParam(description = "用户ID", required = true) int clientId) {
//        // Here you would add code to find nearby facilities based on user address
//        return "附近的药店: 药店A, 地址A; 药店B, 地址B; 药店C, 地址C. 附近的化验所: 化验所X, 地址X; 化验所Y, 地址Y; 化验所Z, 地址Z.";
//    }
//
//    // 存地址到数据库
//    @Tool(description = "把发来的pharmacy_name, pharmacy_address, lab_name, lab_address存到对应用户的prescription和requisition数据库")
//    public String saveAddress(@ToolParam(description = "用户ID", required = true) int clientId,
//                           @ToolParam(description = "药店名称", required = true) String pharmacy_name,
//                           @ToolParam(description = "药店地址", required = true) String pharmacy_address,
//                           @ToolParam(description = "化验所名称", required = true) String lab_name,
//                           @ToolParam(description = "化验所地址", required = true) String lab_address) {
//        // Here you would add code to save the information to a database
//        return "信息已保存: 药店 - " + pharmacy_name + ", 地址 - " + pharmacy_address +
//                "; 化验所 - " + lab_name + ", 地址 - " + lab_address;
//    }
//
//    // 根据指定用户ID查出其完整的prescription和requisition
//    @Tool(description = "根据指定用户ID查出其完整的prescription和requisition")
//    public String getDocuments(@ToolParam(description = "用户ID", required = true) int clientId) {
//        // Here you would add code to fetch the documents from a database
//        return "用户ID: " + clientId + ", Prescription: [药品A, 2盒, 药品B, 1盒], Requisition: [血常规, 尿常规]";
//    }
//
//    // 根据指定用户ID把其完整的prescription和requisition模拟发送传真到名为pharmacy_name的pharmacy机构和名为lab_name的lab机构
//    @Tool(description = "根据指定用户ID把其完整的prescription和requisition模拟发送传真到名为pharmacy_name的pharmacy机构和名为lab_name的lab机构")
//    public String sendFax(@ToolParam(description = "用户ID", required = true) int clientId) {
//        // Here you would add code to send the documents via fax
//        return "已将用户ID " + clientId + " 的prescription发送到xxx药店，requisition发送到xxx化验所。";
//    }
}
