package org.kaaproject.kaa.sandbox.demo;

import org.kaaproject.kaa.common.dto.*;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.sandbox.demo.projects.Platform;
import org.kaaproject.kaa.sandbox.demo.projects.Project;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.verifiers.facebook.config.FacebookVerifierConfig;
import org.kaaproject.kaa.server.verifiers.facebook.config.gen.FacebookAvroConfig;
import org.kaaproject.kaa.server.verifiers.gplus.config.GplusVerifierConfig;
import org.kaaproject.kaa.server.verifiers.gplus.config.gen.GplusAvroConfig;
import org.kaaproject.kaa.server.verifiers.twitter.config.TwitterVerifierConfig;
import org.kaaproject.kaa.server.verifiers.twitter.config.gen.TwitterAvroConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VerifiersDemoBuilder extends AbstractDemoBuilder {
    private static final Logger logger = LoggerFactory.getLogger(VerifiersDemoBuilder.class);

    private static final String TWITTER_CONSUMER_KEY = "01Y9gbsMeGPetye1w9kkNvNMi";
    private static final String TWITTER_CONSUMER_SECRET = "g4Pwh51o7SQlhd3RL6inNF3VxixBURAJDZc494uSISF7yOyJjc";
    private static final String FACEBOOK_APP_ID = "1557997434440423";
    private static final String FACEBOOK_APP_SECRET = "8ff17981ea0cdad3fe387a55c91aa71b";
    private static final String TWITTER_VERIFY_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";
    private static final int MAX_PARALLEL_CONNECTIONS = 5;
    private static final int MIN_PARALLEL_CONNECTIONS = 2;
    private static final Long KEEP_ALIVE_MILLISECONDS = 60000L;

    protected VerifiersDemoBuilder() {
        super();
    }

    @Override
    protected void buildDemoApplicationImpl(AdminClient client) throws Exception {

        logger.info("Loading 'Verifiers Demo Application' data...");

        loginTenantAdmin(client);

        ApplicationDto verifiersApplication = new ApplicationDto();
        verifiersApplication.setName("Verifiers");
        verifiersApplication = client.editApplication(verifiersApplication);

        sdkKey.setApplicationId(verifiersApplication.getId());
        sdkKey.setProfileSchemaVersion(1);
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setLogSchemaVersion(1);
        sdkKey.setTargetPlatform(SdkPlatform.ANDROID);

        loginTenantDeveloper(client);

        logger.info("Creating configuration schema...");
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setApplicationId(verifiersApplication.getId());
        configurationSchema.setName("KaaVerifiersTokens schema");
        configurationSchema.setDescription("Configuration schema for the default Kaa verifiers tokens");
        configurationSchema = client.createConfigurationSchema(configurationSchema, "demo/verifiers/config_schema.avsc");
        logger.info("Configuration schema version: {}", configurationSchema.getMajorVersion());
        sdkKey.setConfigurationSchemaVersion(configurationSchema.getMajorVersion());
        logger.info("Configuration schema was created.");

        TwitterVerifierConfig twitterVerifierConfig = new TwitterVerifierConfig();
        UserVerifierDto twitterUserVerifier = new UserVerifierDto();
        twitterUserVerifier.setApplicationId(verifiersApplication.getId());
        twitterUserVerifier.setName("Twitter verifier");
        twitterUserVerifier.setPluginClassName(twitterVerifierConfig.getPluginClassName());
        twitterUserVerifier.setPluginTypeName(twitterVerifierConfig.getPluginTypeName());

        TwitterAvroConfig twitterAvroConfig = new TwitterAvroConfig();
        twitterAvroConfig.setConsumerKey(TWITTER_CONSUMER_KEY);
        twitterAvroConfig.setConsumerSecret(TWITTER_CONSUMER_SECRET);
        twitterAvroConfig.setMaxParallelConnections(MAX_PARALLEL_CONNECTIONS);
        twitterAvroConfig.setTwitterVerifyUrl(TWITTER_VERIFY_URL);
        twitterUserVerifier.setJsonConfiguration(twitterAvroConfig.toString());
        logger.info("Twitter config: {}", twitterAvroConfig.toString());
        twitterUserVerifier = client.editUserVerifierDto(twitterUserVerifier);

        FacebookVerifierConfig facebookVerifierConfig = new FacebookVerifierConfig();
        UserVerifierDto facebookUserVerifier = new UserVerifierDto();
        facebookUserVerifier.setApplicationId(verifiersApplication.getId());
        facebookUserVerifier.setName("Facebook verifier");
        facebookUserVerifier.setPluginClassName(facebookVerifierConfig.getPluginClassName());
        facebookUserVerifier.setPluginTypeName(facebookVerifierConfig.getPluginTypeName());

        FacebookAvroConfig facebookAvroConfig = new FacebookAvroConfig();
        facebookAvroConfig.setAppId(FACEBOOK_APP_ID);
        facebookAvroConfig.setAppSecret(FACEBOOK_APP_SECRET);
        facebookAvroConfig.setMaxParallelConnections(MAX_PARALLEL_CONNECTIONS);
        facebookUserVerifier.setJsonConfiguration(facebookAvroConfig.toString());
        logger.info("Facebook config: {} ", facebookAvroConfig.toString());
        facebookUserVerifier = client.editUserVerifierDto(facebookUserVerifier);

        GplusVerifierConfig gplusVerifierConfig = new GplusVerifierConfig();
        UserVerifierDto gplusUserVerifier = new UserVerifierDto();
        gplusUserVerifier.setApplicationId(verifiersApplication.getId());
        gplusUserVerifier.setName("Google+ verifier");
        gplusUserVerifier.setPluginClassName(gplusVerifierConfig.getPluginClassName());
        gplusUserVerifier.setPluginTypeName(gplusVerifierConfig.getPluginTypeName());

        GplusAvroConfig gplusAvroConfig = new GplusAvroConfig();
        gplusAvroConfig.setMaxParallelConnections(MAX_PARALLEL_CONNECTIONS);
        gplusAvroConfig.setKeepAliveTimeMilliseconds(KEEP_ALIVE_MILLISECONDS);
        gplusAvroConfig.setMinParallelConnections(MIN_PARALLEL_CONNECTIONS);
        gplusUserVerifier.setJsonConfiguration(gplusAvroConfig.toString());
        logger.info("Google+ config: {} ", facebookAvroConfig.toString());
        gplusUserVerifier = client.editUserVerifierDto(gplusUserVerifier);

        logger.info("Getting endpoint group...");
        EndpointGroupDto baseEndpointGroup = null;
        List<EndpointGroupDto> endpointGroups = client.getEndpointGroups(verifiersApplication.getId());
        if (endpointGroups.size() == 1 && endpointGroups.get(0).getWeight() == 0) {
            baseEndpointGroup = endpointGroups.get(0);
        }
        if (baseEndpointGroup == null) {
            logger.debug("Can't get default endpoint group for verifiers demo application!");
            throw new RuntimeException("Can't get default endpoint group for verifiers demo application!");
        }

        logger.info("Creating base configuration...");
        ConfigurationDto baseConfiguration = new ConfigurationDto();
        baseConfiguration.setApplicationId(verifiersApplication.getId());
        baseConfiguration.setEndpointGroupId(baseEndpointGroup.getId());
        baseConfiguration.setSchemaId(configurationSchema.getId());
        baseConfiguration.setMajorVersion(configurationSchema.getMajorVersion());
        baseConfiguration.setMinorVersion(configurationSchema.getMinorVersion());
        baseConfiguration.setDescription("Base verifiers demo configuration");
        String body = generateBody(twitterUserVerifier.getVerifierToken(),
                facebookUserVerifier.getVerifierToken(), gplusUserVerifier.getVerifierToken());
        baseConfiguration.setBody(body);
        baseConfiguration.setStatus(UpdateStatus.INACTIVE);

        logger.info("Configuration body: {}", body);
        logger.info("Base configuration: {}", baseConfiguration.toString());
        baseConfiguration = client.editConfiguration(baseConfiguration);
        logger.info("Base configuration was created: {}", baseConfiguration.toString());
        logger.info("Activating base configuration for [{}] id...", baseConfiguration.getId());
        client.activateConfiguration(baseConfiguration.getId());
        logger.info("Base configuration was activated");
        logger.info("Finished loading 'Verifiers Demo Application' data.");
    }

    @Override
    protected void setupProjectConfigs() {
        Project projectConfig = new Project();
        projectConfig.setId("verifiers_demo");
        projectConfig.setName("Verifiers Demo");
        projectConfig.setDescription("Verifiers application on android platform demonstrating user verification system");
        projectConfig.setPlatform(Platform.ANDROID);
        projectConfig.setSourceArchive("android/verifiers_demo.tar.gz");
        projectConfig.setProjectFolder("verifiers_demo/VerifiersDemo");
        projectConfig.setSdkLibDir("verifiers_demo/VerifiersDemo/libs");
        projectConfig.setDestBinaryFile("verifiers_demo/VerifiersDemo/bin/VerifiersDemo-debug.apk");
        projectConfigs.add(projectConfig);
    }

    private String generateBody(String twitterVerifierToken, String facebookVerifierToken, String gplusVerifierToken) {
        logger.info("Generating body... twitterVerifierToken: {}", twitterVerifierToken);

        return "{\n" +
                "  \"twitterKaaVerifierToken\" : {\n" +
                "    \"string\" : \"" + twitterVerifierToken + "\"\n" +
                "  },\n" +
                "  \"facebookKaaVerifierToken\" : {\n" +
                "    \"string\" : \"" + facebookVerifierToken + "\"\n" +
                "  },\n" +
                "  \"googleKaaVerifierToken\" : {\n" +
                "    \"string\" : \"" + gplusVerifierToken + "\"\n" +
                "  },\n" +
                " \"__uuid\": null" +
                "}";
    }
}
