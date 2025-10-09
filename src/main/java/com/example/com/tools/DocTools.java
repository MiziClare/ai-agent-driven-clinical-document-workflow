package com.example.com.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocTools {
    @Tool
    public String helloTool(String name) {
        return "Hello, " + name + "!";
    }
}
