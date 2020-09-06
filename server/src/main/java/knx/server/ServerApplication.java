package knx.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan
@EnableAutoConfiguration
@EnableScheduling
public class ServerApplication {

    /**
     * <h1>Starting</h1>
     *
     * <pre>
     * mvn spring-boot:run
     * </pre>
     *
     * <h1>Stopping</h1>
     *
     * The spring boot application has to be shut down gracefully as otherwise the
     * webserver keep listening on ports and the database files keep beeing locked
     * by processes. The recommended way is to shutdown using the actuator: You can
     * use the shutdown actuator
     *
     * <pre>
     * curl -X POST 127.0.0.1:8080/actuator/shutdown
     * </pre>
     *
     * Alternatively, you can find the PID of the process listening on port 8080 and
     * kill the process:
     *
     * <pre>
     * netstat -ano|findstr "PID: 8080"
     * taskkill /pid <PID_GOES_HERE> /f
     * </pre>
     *
     * @param args
     */
    public static void main(final String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
