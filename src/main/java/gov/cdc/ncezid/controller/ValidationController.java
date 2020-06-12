package gov.cdc.ncezid.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import gov.cdc.ncezid.model.*;
import gov.cdc.ncezid.persist.MinioException;
import gov.cdc.ncezid.service.DroolsService;
import gov.cdc.ncezid.service.InvalidConfiguratonException;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;
import io.micronaut.http.hateoas.JsonError;
import io.netty.util.internal.StringUtil;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller("/validate")
@Introspected
public class ValidationController {

	private final Logger logger = LoggerFactory.getLogger(ValidationController.class);

	private DroolsService droolsService;
	private CDCLoggerHelper cdcLoggerHelper;


	@Inject
	ValidationController(DroolsService droolsService, CDCLoggerHelper cdcLoggerService) {
		this.droolsService = droolsService;
		this.cdcLoggerHelper = cdcLoggerService;
	}

	@Post(value = "/{groupName}/csv{?metadata}", consumes = MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public HttpResponse<?> validateCsv(String groupName, @Nullable @QueryValue String[] metadata, @Body String contents) throws MinioException, InvalidConfiguratonException {
		logger.info("AUDIT - Started validating CSV");

		if (droolsService.getRuleFilenames(groupName).size() == 0) {
			return HttpResponse.badRequest(new JsonError("No rule files have been uploaded"));
		}

		Map<String, Object> metaDataKeyValuePairs = new HashMap<>();
		if (metadata != null) {
			for (String keyValueType : metadata) {
				// Split the string into key, value and type separated by commas
				final String parts[] = keyValueType.split(",");
				if (parts.length != 3) {
					return HttpResponse
							.badRequest(new JsonError("Invalid metadata format provided (format is key,value,type)"));
				}
				final String key = parts[0].trim();
				final String value = parts[1].trim();
				final String type = parts[2].trim().toLowerCase();

				if (StringUtil.isNullOrEmpty(key) || value == null)
					return HttpResponse.badRequest(new JsonError("Invalid key/pair provided"));

				Object valueAsObject;
				try {
					switch (type.toUpperCase(Locale.ENGLISH)) {
						case "INTEGER":
							valueAsObject = Integer.parseInt(value);
							break;
						case "FLOAT":
							valueAsObject = Float.parseFloat(value);
							break;
						case "DOUBLE":
							valueAsObject = Double.parseDouble(value);
							break;
						case "STRING":
							valueAsObject = value;
							break;
						default:
							return HttpResponse.badRequest(new JsonError(
									"Invalid metadata type provided.  Only integer, float, double and string types are supported."));
					}
				} catch (NumberFormatException e) {
					return HttpResponse
							.badRequest(new JsonError("Error parsing the metadata value as the type provided"));
				}

				metaDataKeyValuePairs.put(key, valueAsObject);
			}
		}

		// Initialize the rules before doing anything else
		KieSession kSession;
		try {
			kSession = droolsService.initializeSession(groupName);
		} catch (RuntimeException e) {
			return HttpResponse.serverError(new JsonError("Exception: " + e));
		}

		// Convert contents into CSVFile
		CSVFile csvFile = convertToCSVFile(contents);
		csvFile.setMetadata(metaDataKeyValuePairs);
		droolsService.insert(kSession, csvFile);
		HttpResponse<?> response;
		try {
			droolsService.fireRules(kSession);
			response = getValidationReport(kSession);
		} catch (ValidationException e) {
			response = HttpResponse.status(e.getHttpStatus()).body(new JsonError(e.getMessage()));
		}

		// Release the session after we've obtained the validation report. Releasing the
		// session also clears the validation report data.
		droolsService.releaseSession(kSession);

		logger.info("AUDIT - Done validating CSV, responding with report");

		return response;
	}

	@Post(value = "/{groupName}/json", consumes = MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public HttpResponse<?> validateJson(String groupName, @Body String jsonString) throws MinioException, InvalidConfiguratonException {
		logger.info("AUDIT - Started validating JSON");

		if (droolsService.getRuleFilenames(groupName).size() == 0) {
			return HttpResponse.badRequest(new JsonError("No rule files have been uploaded"));
		}

		// Initialize the rules before doing anything else
		KieSession kSession;
		try {
			kSession = droolsService.initializeSession(groupName);
		} catch (RuntimeException e) {
			return HttpResponse.serverError(new JsonError("Exception: " + e));
		}

		Gson gson = new Gson();
		JsonFile jsonFile = gson.fromJson(jsonString, JsonFile.class);
		jsonFile.setJsonString(jsonString);

		droolsService.insert(kSession, jsonFile);
		HttpResponse<?> response;
		try {
			droolsService.fireRules(kSession);
			response = getValidationReport(kSession);
		} catch (ValidationException e) {
			response = HttpResponse.status(e.getHttpStatus()).body(new JsonError(e.getMessage()));
		}

		// Release the session after we've obtained the validation report. Releasing the
		// session also clears the validation report data.
		droolsService.releaseSession(kSession);

		logger.info("AUDIT - Done validating JSON, responding with report");

		return response;
	}

	private HttpResponse<?> getValidationReport(KieSession kSession) {
		HttpResponse<?> response;
		Object customReport = droolsService.getCustomReport(kSession);
		if (customReport != null) {
			try {
				// Note, we use the following instead of simply, response = HttpResponse.ok(customReport) to ensure the details ket is always provided, even if it is an empty list. 
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.setDateFormat(new SimpleDateFormat("M/d/yyyy"));
				response = HttpResponse.ok(objectMapper.writeValueAsString(customReport));
			} catch (JsonProcessingException e) {
				response = HttpResponse.serverError("Failed to generate the custome report in JSON");
			}
		} else {
			ValidationReport validationReport = new ValidationReport(droolsService.getErrors(kSession));
			response = HttpResponse.ok(validationReport);
		}
		return response;
	}

	private CSVFile convertToCSVFile(String contents) {
		CSVFile csvFile = new CSVFile();
		int lineno = 0;
		Scanner scanner = new Scanner(contents);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (lineno++ == 0)
				continue;
			String[] fields = line.split(",");
			UnformattedRow row = new UnformattedRow();
			row.setFields(Arrays.asList(fields));
			csvFile.addRow(row);
		}
		scanner.close();
		return csvFile;
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
}

