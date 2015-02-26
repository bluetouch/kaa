package org.kaaproject.kaa.sandbox.demo;

import org.kaaproject.kaa.common.dto.ApplicationDto;
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

public class VerifiersDemoBuilder extends AbstractDemoBuilder {
    private static final Logger logger = LoggerFactory.getLogger(VerifiersDemoBuilder.class);

    private static final String TWITTER_VERIFIER_TOKEN  = "00000000000000000000";
    private static final String FACEBOOK_VERIFIER_TOKEN = "11111111111111111111";
    private static final String GPLUS_VERIFIER_TOKEN    = "22222222222222222222";
    private static final String TWITTER_CONSUMER_KEY = "01Y9gbsMeGPetye1w9kkNvNMi";
    private static final String TWITTER_CONSUMER_SECRET = "g4Pwh51o7SQlhd3RL6inNF3VxixBURAJDZc494uSISF7yOyJjc";
    private static final String FACEBOOK_APP_ID = "1557997434440423";
    private static final String FACEBOOK_APP_SECRET = "8ff17981ea0cdad3fe387a55c91aa71b";


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
        sdkKey.setConfigurationSchemaVersion(1);
        sdkKey.setNotificationSchemaVersion(1);
        sdkKey.setLogSchemaVersion(1);
        sdkKey.setTargetPlatform(SdkPlatform.ANDROID);

        loginTenantDeveloper(client);

        TwitterVerifierConfig twitterVerifierConfig = new TwitterVerifierConfig();
        UserVerifierDto twitterUserVerifier = new UserVerifierDto();
        twitterUserVerifier.setApplicationId(verifiersApplication.getId());
        twitterUserVerifier.setName("Twitter verifier");
        twitterUserVerifier.setPluginClassName(twitterVerifierConfig.getPluginClassName());
        twitterUserVerifier.setPluginTypeName(twitterVerifierConfig.getPluginTypeName());
        twitterUserVerifier.setVerifierToken(TWITTER_VERIFIER_TOKEN);

        TwitterAvroConfig twitterAvroConfig = new TwitterAvroConfig();
        twitterAvroConfig.setConsumerKey(TWITTER_CONSUMER_KEY);
        twitterAvroConfig.setConsumerSecret(TWITTER_CONSUMER_SECRET);
        twitterUserVerifier.setJsonConfiguration(twitterAvroConfig.toString());
        twitterUserVerifier = client.editUserVerifierDto(twitterUserVerifier);
        
        FacebookVerifierConfig facebookVerifierConfig = new FacebookVerifierConfig();
        UserVerifierDto facebookUserVerifier = new UserVerifierDto();
        facebookUserVerifier.setApplicationId(verifiersApplication.getId());
        facebookUserVerifier.setName("Facebook verifier");
        facebookUserVerifier.setPluginClassName(facebookVerifierConfig.getPluginClassName());
        facebookUserVerifier.setPluginTypeName(facebookVerifierConfig.getPluginTypeName());
        facebookUserVerifier.setVerifierToken(FACEBOOK_VERIFIER_TOKEN);

        FacebookAvroConfig facebookAvroConfig = new FacebookAvroConfig();
        facebookAvroConfig.setAppId(FACEBOOK_APP_ID);
        facebookAvroConfig.setAppSecret(FACEBOOK_APP_SECRET);
        facebookUserVerifier.setJsonConfiguration(facebookAvroConfig.toString());
        facebookUserVerifier = client.editUserVerifierDto(facebookUserVerifier);

        GplusVerifierConfig gplusVerifierConfig = new GplusVerifierConfig();
        UserVerifierDto gplusUserVerifier = new UserVerifierDto();
        gplusUserVerifier.setApplicationId(verifiersApplication.getId());
        gplusUserVerifier.setName("Google+ verifier");
        gplusUserVerifier.setPluginClassName(gplusVerifierConfig.getPluginClassName());
        gplusUserVerifier.setPluginTypeName(gplusVerifierConfig.getPluginTypeName());
        gplusUserVerifier.setVerifierToken(GPLUS_VERIFIER_TOKEN);

        GplusAvroConfig gplusAvroConfig = new GplusAvroConfig();
        gplusUserVerifier.setJsonConfiguration(gplusAvroConfig.toString());
        gplusUserVerifier = client.editUserVerifierDto(gplusUserVerifier);

        sdkKey.setDefaultVerifierToken(twitterUserVerifier.getVerifierToken());

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
}
