package gov.cdc.ncezid.controller;

import gov.cdc.ncezid.rest.security.S2SAuth;
import gov.cdc.ncezid.rest.security.ServiceNotAuthorizedException;
import gov.cdc.ncezid.model.ERROR_CODES;
import gov.cdc.ncezid.model.ErrorReceipt;
import gov.cdc.ncezid.model.JsonResult;
import gov.cdc.ncezid.persist.MinioException;
import gov.cdc.ncezid.service.DroolsService;
import gov.cdc.ncezid.service.InvalidConfiguratonException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Controller("/admin") 
public class AdminController {
    private final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final S2SAuth s2sauth;
    private DroolsService droolsService;
    private CDCLoggerHelper cdcLoggerHelper;

    @Inject
    public AdminController(DroolsService droolsService, CDCLoggerHelper cdcLogger, S2SAuth auth) {
        this.droolsService = droolsService;
        this.cdcLoggerHelper = cdcLogger;
        this.s2sauth = auth;
    }

    @Get("/rules/{groupName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> getRuleFilenames(String groupName) throws MinioException, InvalidConfiguratonException {
        return droolsService.getRuleFilenames(groupName);
    }

    @Post(value = "/rules/{groupName}/{filename}", consumes = MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    // @ApiResponse(responseCode = "200", description = "Rules file uploaded", content = @Content(schema = @Schema(implementation = JsonResult.class)))
    public HttpResponse<?> uploadRules(@Header("s2s-token") String token ,String groupName, String filename, @Body String ruleContent) throws MinioException, ServiceNotAuthorizedException {
        s2sauth.checkS2SCredentials(token);
        droolsService.addRuleFile(groupName, filename, ruleContent);
        return HttpResponse.ok(new JsonResult("Accepted"));
    }

    @Delete("/rules/{groupName}")
    @Produces(MediaType.APPLICATION_JSON)
    // @ApiResponse(responseCode = "200", description = "Rules files removed", content = @Content(schema = @Schema(implementation = JsonResult.class)))
    public JsonResult removeAllRules(@Header("s2s-token") String token ,String groupName) throws MinioException, ServiceNotAuthorizedException {
        s2sauth.checkS2SCredentials(token);
        droolsService.removeAllRuleFiles(groupName);
        return new JsonResult("Accepted");
    }

    @Error(exception = MinioException.class)
    protected HttpResponse<ErrorReceipt> handleMinioError(HttpRequest request, MinioException ex) {
        logger.error("Exception thrown: " + ex.getMessage());
        ErrorReceipt error = new ErrorReceipt(ERROR_CODES.INTERNAL_SERVER_ERROR, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.getCode(), request.getPath(), ex.getMessage());
        cdcLoggerHelper.logErrorToCDC(ex, request);
        return HttpResponse.serverError(error);
    }

    @Error(exception = InvalidConfiguratonException.class)
    protected HttpResponse<ErrorReceipt> handleInvalidGroup(HttpRequest request, InvalidConfiguratonException ex) {
        logger.error("Exception thrown: " + ex.getMessage());
        ErrorReceipt error = new ErrorReceipt(ERROR_CODES.BAD_REQUEST, ex.getMessage(), HttpStatus.BAD_REQUEST.getCode(), request.getPath(), ex.getMessage());
        return HttpResponse.badRequest(error);
    }

    @Error(exception = ServiceNotAuthorizedException.class)
    protected HttpResponse<ErrorReceipt> handleAuthorizationErrors(HttpRequest request, ServiceNotAuthorizedException e) {
        logger.error("Exception thrown: " + e.getMessage());
        ErrorReceipt error = new ErrorReceipt(ERROR_CODES.UNAUTHORIZED, e.getMessage(), HttpStatus.UNAUTHORIZED.getCode(), request.getPath(), e.getMessage());
        return HttpResponse.badRequest(error);

    }
}