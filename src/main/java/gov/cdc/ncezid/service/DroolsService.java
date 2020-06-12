package gov.cdc.ncezid.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import gov.cdc.ncezid.persist.MinioException;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.ConsequenceException;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cdc.ncezid.model.CustomReport;
import gov.cdc.ncezid.model.RuleFile;
import gov.cdc.ncezid.model.ValidationException;
import gov.cdc.ncezid.model.ValidationIssue;
import gov.cdc.ncezid.persist.MinioManager;

@Singleton
public class DroolsService {

    private final Logger logger = LoggerFactory.getLogger(DroolsService.class);

    // Maps groupNames to kContainers
    private Map<String, KieContainer> kContainerMap = new HashMap<>();

    // Maps the kSession identifier to the list of issues for that session
    private Map<Long, List<ValidationIssue>> issuesMap = new HashMap<>();

    // Maps kSession identifier to the custom report for that session
    private Map<Long, Object> customReportMap = new HashMap<>();

    private MinioManager minioManager;

    private Set<String> loadingGroupNamesSet;

    @Inject
    public DroolsService(MinioManager minioManager) {
        this.minioManager = minioManager;
        
        // Create a concurrent hash set from a concurrent hash map key set
        this.loadingGroupNamesSet = ConcurrentHashMap.newKeySet();
    }

    public void addRuleFile(String groupName, String filename, String ruleContent) throws MinioException {
        minioManager.uploadRules(groupName, filename, ruleContent);
        loadRulesIntoKieContainer(groupName);
    }

    private void loadRulesIntoKieContainer(String groupName) throws MinioException {
        if (loadingGroupNamesSet.contains(groupName)) {
            do {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    logger.error("Interrupted waiting for the rules to finish loading");
                    return;
                }
            } while (loadingGroupNamesSet.contains(groupName));

            return;
        }
        loadingGroupNamesSet.add(groupName);

        Collection<RuleFile> rulesContentSet;
        try {
            rulesContentSet = minioManager.getRules(groupName);
        } catch (Exception e) {
            loadingGroupNamesSet.remove(groupName);
            throw e;
        }

        KieHelper kieHelper = new KieHelper();
        rulesContentSet.forEach(drl -> kieHelper.addContent(drl.getContent(), "gov/cdc/ncezid/" + drl.getFilename()));
        Results results = kieHelper.verify();

        if (results.hasMessages(Message.Level.ERROR)) {
            StringBuilder sb = new StringBuilder();
            for (Message result : results.getMessages()) {
                sb.append(result.getText() + "\n");
            }
            loadingGroupNamesSet.remove(groupName);
            throw new RuntimeException("Build Errors:\n" + sb.toString());
        }
        KieContainer kContainer = kieHelper.getKieContainer();
        kContainerMap.put(groupName, kContainer);

        loadingGroupNamesSet.remove(groupName);
    }

    public void removeAllRuleFiles(String groupName) throws MinioException {
//        try {
            minioManager.removeRules(groupName);
//        } catch (Exception e) {
//            logger.error("Failed to remove all rules files: " + e.getLocalizedMessage());
//        }
    }

    public Collection<String> getRuleFilenames(String groupName) throws MinioException, InvalidConfiguratonException {
            return minioManager.getRuleFilenames(groupName);
    }

    public KieSession initializeSession(String groupName) throws MinioException {
        if (kContainerMap.get(groupName) == null) {
            loadRulesIntoKieContainer(groupName);
        }
        // Return the new KieSession. Each KieSession will have a unique identifier.
        return kContainerMap.get(groupName).newKieSession();
    }

    public void insert(KieSession kSession, Object content) {
        kSession.insert(content);
    }

    public void fireRules(KieSession kSession) throws ValidationException {
        logger.info("AUDIT - Started executing rules with session id = " + kSession.getIdentifier());
        long startTime = System.nanoTime();

        // Setup the globals shared between rules files and Java env
        List<ValidationIssue> issues = new ArrayList<>();
        CustomReport customReport = new CustomReport();
        issuesMap.put(kSession.getIdentifier(), issues);
        customReportMap.put(kSession.getIdentifier(), customReport);
        kSession.setGlobal("issues", issues);
        kSession.setGlobal("customReport", customReport);

        try {
            kSession.fireAllRules();
        } catch (ConsequenceException e) {
            if (e.getCause() instanceof ValidationException)
                throw (ValidationException) e.getCause();
            else
                throw e;
        }

        long endTime = System.nanoTime();
        double timeElapsed = endTime - startTime;
        logger.info("AUDIT - Finished executing rules with session id = " + kSession.getIdentifier() + ", took " + timeElapsed / 1e6 + " ms");
    }

    public void releaseSession(KieSession kSession) {
        issuesMap.remove(kSession.getIdentifier());
        customReportMap.remove(kSession.getIdentifier());
        kSession.dispose();
    }

    public List<ValidationIssue> getErrors(KieSession kSession) {
        return issuesMap.get(kSession.getIdentifier());
    }

    public Object getCustomReport(KieSession kSession) {
        return customReportMap.get(kSession.getIdentifier());
    }
}