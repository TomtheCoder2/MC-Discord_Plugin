package minecraft.plugin.springbackend.test2.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@ComponentScan("minecraft.plugin.springbackend.test2")
public class DemoApplication {

//    private final MyService myService;
//
//    public DemoApplication(MyService myService) {
//        this.myService = myService;
//    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    public void start() {
        SpringApplication.run(DemoApplication.class);
    }

//    @GetMapping("/")
//    public String home() {
//        return myService.message();
//    }
}