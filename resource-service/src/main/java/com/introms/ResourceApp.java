package com.introms;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ResourceApp {
    public static void main(String[] args) {
        SpringApplication.run(ResourceApp.class,args);
    }

    @Autowired
    DiscoveryClient discoveryClient;

    @PostConstruct
    public void dumpInstances(){
        System.out.println("Known Services: " + discoveryClient.getServices());
        discoveryClient.getInstances("song-service")
                .forEach(s -> System.out.println(s.getUri()));
    }
}