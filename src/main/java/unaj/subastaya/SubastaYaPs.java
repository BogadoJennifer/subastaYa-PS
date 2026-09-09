package unaj.subastaya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubastaYaPs {

    public static void main(String[] args) {
        SpringApplication.run(SubastaYaPs.class, args);
    }

}
