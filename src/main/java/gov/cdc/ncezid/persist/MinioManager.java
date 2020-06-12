package gov.cdc.ncezid.persist;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import java.security.InvalidKeyException;

import gov.cdc.ncezid.service.InvalidConfiguratonException;
import io.minio.PutObjectOptions;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import gov.cdc.ncezid.model.RuleFile;
import io.micronaut.context.annotation.Value;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;

@Singleton
public class MinioManager {

    private final Logger logger = LoggerFactory.getLogger(MinioManager.class);

    private static final String bucketNamePrefix = "validation-drools";

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accesskey}")
    private String accessKey;

    @Value("${minio.secretkey}")
    private String secretKey;

    private MinioClient minioClient;

    @PostConstruct
    public void initalize() throws InvalidEndpointException, InvalidPortException {
//        if (StringUtil.isNullOrEmpty(this.endpoint) || StringUtil.isNullOrEmpty(this.accessKey)
//                || StringUtil.isNullOrEmpty(this.secretKey)) {
//            throw new RuntimeException(
//                    "Please check server environment variables, MINIO_ENDPOINT, MINIO_ACCESS_KEY and/or MINIO_SECRET_KEY not set.");
//        }

        logger.info("AUDIT - Env var MINIO_ENDPOINT = " + endpoint);
        this.minioClient = new MinioClient(endpoint, accessKey, secretKey);
    }

    private String getBucketName(String groupName) {
        return bucketNamePrefix + "-" + groupName;
    }

    public void uploadRules(String groupName, String filename, String content) throws MinioException {

        try {
            final String bucketName = getBucketName(groupName);

            if (!minioClient.bucketExists(bucketName))
                minioClient.makeBucket(bucketName);

            ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes("UTF-8"));

            PutObjectOptions options = new PutObjectOptions(content.length(), -1);
            minioClient.putObject(bucketName, filename, bais, options);
            //minioClient.putObject(bucketName, filename, bais, Long.valueOf(bais.available()), null, null,  "application/octet-stream");
            bais.close();
        } catch (InvalidBucketNameException | NoSuchAlgorithmException | XmlParserException | InsufficientDataException | IOException | InvalidKeyException  | ErrorResponseException | InternalException  | RegionConflictException e) {
            throw new MinioException(e.getMessage());
        } catch (InvalidResponseException e) {
            e.printStackTrace();
        }
    }

    public List<RuleFile> getRules(String groupName) throws MinioException {

        try {
            List<RuleFile> rules = new ArrayList<>();

            final String bucketName = getBucketName(groupName);

            if (minioClient.bucketExists(bucketName)) {
                Iterable<Result<Item>> myObjects = minioClient.listObjects(bucketName);
                for (Result<Item> result : myObjects) {
                    Item item = result.get();
                    String filename = item.objectName();
                    String content = getRules(groupName, filename);
                    rules.add(new RuleFile(filename, content));
                }
            }

            return rules;
        } catch (InvalidBucketNameException | NoSuchAlgorithmException | XmlParserException| InvalidResponseException| InsufficientDataException | IOException | InvalidKeyException  | ErrorResponseException | InternalException e) {
            throw new MinioException(e.getMessage());
        }
    }

    public List<String> getRuleFilenames(String groupName) throws MinioException, InvalidConfiguratonException {
        List<String> ruleFilenames = new ArrayList<>();
        final String bucketName = getBucketName(groupName);
        try {
            if (minioClient.bucketExists(bucketName)) {
                Iterable<Result<Item>> myObjects = minioClient.listObjects(bucketName);
                for (Result<Item> result : myObjects) {
                    Item item = result.get();
                    String filename = item.objectName();
                    ruleFilenames.add(filename);
                }
                return ruleFilenames;
            } else {
                throw new InvalidConfiguratonException("configuration for " + groupName + " not Found.");
            }
        } catch (InvalidBucketNameException | NoSuchAlgorithmException | XmlParserException| InvalidResponseException| InsufficientDataException | IOException | InvalidKeyException  | ErrorResponseException | InternalException e) {
            throw new MinioException(e.getMessage());
        }
    }

    private String getRules(String groupName, String filename) throws MinioException {
        final String bucketName = getBucketName(groupName);
        try (InputStream stream = minioClient.getObject(bucketName, filename)) {
            //InputStream stream = minioClient.getObject(bucketName, filename);
            // Read the input stream and print to the console till EOF.
            StringBuilder strb = new StringBuilder();
            byte[] buf = new byte[16384];
            int bytesRead;
            while ((bytesRead = stream.read(buf, 0, buf.length)) >= 0) {
                strb.append(new String(buf, 0, bytesRead, StandardCharsets.UTF_8));
            }
            // Close the input stream. MSC - closed using Try with....
            //stream.close();
            return strb.toString();
        } catch (InvalidBucketNameException | NoSuchAlgorithmException | XmlParserException| InvalidResponseException| InsufficientDataException | IOException | InvalidKeyException  | ErrorResponseException | InternalException e) {
            throw new MinioException(e.getMessage());
        }
    }

    public void removeRules(String groupName) throws MinioException {
        try {
            final String bucketName = getBucketName(groupName);
            if (minioClient.bucketExists(bucketName)) {
                // First we have to remove all the files in the bucket before it can be deleted.
                Iterable<Result<Item>> myObjects = minioClient.listObjects(bucketName);
                for (Result<Item> result : myObjects) {
                    Item item = result.get();
                    minioClient.removeObject(bucketName, item.objectName());
                }
                minioClient.removeBucket(bucketName);
            }
        } catch (InvalidBucketNameException | NoSuchAlgorithmException | XmlParserException| InvalidResponseException| InsufficientDataException | IOException | InvalidKeyException  | ErrorResponseException | InternalException e) {
            throw new MinioException(e.getMessage());
        }
    }

}