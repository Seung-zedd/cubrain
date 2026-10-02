package com.cubrain.springboot_starter_auth.global.config.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AiConfigTest {

    private static final String MODEL_KEY = "langchain4j.google-ai-gemini.chat-model.model-name";

    @Test
    void chatModelInitializesWithConfiguredModelName() {
        AiConfig config = new AiConfig();
        ReflectionTestUtils.setField(config, "chatApiKey", "dummy-key");
        ReflectionTestUtils.setField(config, "chatModelName", "gemini-3.8-flash");
        ReflectionTestUtils.setField(config, "chatTemperature", 1.0);

        ChatLanguageModel model = config.chatLanguageModel();

        assertInstanceOf(GoogleAiGeminiChatModel.class, model);
    }

    @Test
    void prodModelDefaultsToSupportedFlashModel() {
        assertEquals("gemini-3.8-flash", resolveProdModel(new MockEnvironment()));
    }

    @Test
    void prodModelCanBeOverriddenWithGeminiModelEnv() {
        MockEnvironment env = new MockEnvironment().withProperty("GEMINI_MODEL", "gemini-3.7-flash");
        assertEquals("gemini-3.7-flash", resolveProdModel(env));
    }

    private String resolveProdModel(MockEnvironment env) {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-prod.yml"));
        Properties props = yaml.getObject();
        return env.resolveRequiredPlaceholders(props.getProperty(MODEL_KEY));
    }
}
