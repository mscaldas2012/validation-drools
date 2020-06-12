package gov.cdc.ncezid.controller;

import gov.cdc.ncezid.model.HEALTH_STATUS;
import gov.cdc.ncezid.model.HealthReceipt;
import gov.cdc.ncezid.persist.MinioException;
import gov.cdc.ncezid.persist.MinioManager;
import gov.cdc.ncezid.service.InvalidConfiguratonException;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Controller("/info")
public class InfoController {

    private static final String TAG = InfoController.class.getSimpleName();

    private final Logger logger = LoggerFactory.getLogger(InfoController.class);
    
    private String version;
    private MinioManager minioManager;


    @Inject
    public InfoController(MinioManager minio) {
        this.minioManager = minio;
        //this.version = getVersionFromPom();
    }

    @Get("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "Hello! I am alive.\nYou pinged me at "
                + LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
    }

    @Get("/version")
    @Produces(MediaType.TEXT_PLAIN)
    public String getVersion() {
        return "Version: " + version;
    }



    @Get("/health")
    public HealthReceipt health() {
        HealthReceipt health = new HealthReceipt();
        try {
            minioManager.getRuleFilenames("dprp");
            health.setMinioStatus(HEALTH_STATUS.OK);
        } catch (MinioException | InvalidConfiguratonException e) {
            //e.printStackTrace()
            health.setMinioErrorMessage(e.getMessage());
            health.setMinioStatus(HEALTH_STATUS.DOWN);
        }
        if (HEALTH_STATUS.OK.equals(health.getMinioStatus()))
            health.setOverallStatus(HEALTH_STATUS.OK);
        else
            health.setOverallStatus(HEALTH_STATUS.UNHEALTHY);
        return health;
    }

}
