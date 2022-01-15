package minecraft.plugin.springbackend.test2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import static minecraft.plugin.springbackend.test2.Config.setVars;
import static minecraft.plugin.utils.Log.*;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
public class Main {
    public static void main(String[] args) {
        setVars();
        System.out.println("Start Server...");
        log(Main.class.getClassLoader().getResource("defaultSettings.json").toString());
        log(Main.class.getClassLoader().getResource("META-INF/spring.factories").toString());
        log(Main.class.getClassLoader().getResource("META-INF/NOTICE.md").toString());
        log("--- Spring App ---");
        SpringApplication.run(Main.class, args);
    }
}
