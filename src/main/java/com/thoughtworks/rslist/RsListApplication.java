package com.thoughtworks.rslist;

import com.thoughtworks.rslist.api.RsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RsListApplication {

    public static Logger logger = LoggerFactory.getLogger(RsController.class);

    public static void main(String[] args) {
        SpringApplication.run(RsListApplication.class, args);
    }
}
