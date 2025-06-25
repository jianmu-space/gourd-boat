package space.jianmu.gourdboat.bootstrap;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import io.github.cdimascio.dotenv.Dotenv;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        System.out.println("JVM 当前工作目录: " + System.getProperty("user.dir"));

        Dotenv dotenv = Dotenv.configure()
                .directory("../../")
                .filename(".env.local")
                .ignoreIfMissing()
                .load();
        Map<String, Object> map = new HashMap<>();
        dotenv.entries().forEach(entry -> map.put(entry.getKey(), entry.getValue()));
        environment.getPropertySources().addFirst(new MapPropertySource("dotenv", map));

        // 将map格式化为json后输出
        try {
            // 尝试使用Jackson进行格式化
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
            System.out.println("Dotenv变量（JSON格式）: \n" + json);
        } catch (Exception e) {
            System.out.println("Dotenv变量转JSON失败: " + e.getMessage());
        }
        System.out.println("[EncryptionService]====" + environment.getProperty("EncryptionService"));
        System.out.println("DotenvEnvironmentPostProcessor: 已注入 .env.local 环境变量");
        System.out.println("Dotenv变量: SPRING_DATASOURCE_URL=" + environment.getProperty("SPRING_DATASOURCE_URL"));
    }
} 