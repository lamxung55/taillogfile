package com.vcb.tlf;

import com.vcb.tlf.scheduler.TailLogScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class Application implements CommandLineRunner {

    @Autowired
    TailLogScheduler tailLogScheduler;
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        ApplicationContext applicationContext = app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        tailLogScheduler.tail();
    }
}
