package space.jianmu.gourdboat.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "space.jianmu.gourdboat.bootstrap",
    "space.jianmu.gourdboat.interfaces",
    "space.jianmu.gourdboat.application",
    "space.jianmu.gourdboat.infrastructure"
})
@EntityScan(basePackages = {
    "space.jianmu.gourdboat.infrastructure.persistence"
})
@EnableJpaRepositories(basePackages = {
    "space.jianmu.gourdboat.infrastructure.persistence"
})
public class GourdBoatApplication {

    public static void main(String[] args) {
        SpringApplication.run(GourdBoatApplication.class, args);
    }
} 