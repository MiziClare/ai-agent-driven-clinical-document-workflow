package com.example.com.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocTools {

    @Tool
    public String helloTool(String name) {
        return "Hello, " + name + "!";
    }

    @Tool(description = "查询城市北京明天的天气")
    public String getBeijingWeather(@ToolParam(description = "要查询的城市名", required = false) String city) {
        return city + " 明天多云转晴，最高气温 27°C";
    }

    @Tool(description = "查询城市合肥明天的天气")
    public String getHeifeiWeather(@ToolParam(description = "要查询的城市名", required = false) String city) {
        return city + " 明天下雨，最高气温 21°C";
    }
}
