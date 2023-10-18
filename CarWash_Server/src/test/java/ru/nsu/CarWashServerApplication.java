package ru.nsu;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.nsu.carwash_server.services.FileServiceIml;

import javax.annotation.Resource;


@SpringBootApplication
public class CarWashServerApplication implements CommandLineRunner {

    @Resource
    FileServiceIml storageService;

    public static void main(String[] args) {
        SpringApplication.run(CarWashServerApplication.class, args);
    }

    @Override
    public void run(String... arg) {
//    storageService.deleteAll();
        storageService.init();
    }
}
