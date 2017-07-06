package com.taboola.cronyx.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taboola.cronyx.Cron;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.TriggerDefinition;
import com.taboola.cronyx.testing.web.CronyxTestingWebApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;

import static com.taboola.cronyx.TriggerDefinitionBuilder.cron;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = CronyxTestingWebApplication.class)
public class CronyxRestControllerIntegrationTest {

    @Value("${local.server.port}")
    private int serverPort;
    @Resource
    private ObjectMapper mapper;
    private TestRestTemplate testRest = new TestRestTemplate();

    @Before
    public void setup() {
        testRest.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return response.getStatusCode() != HttpStatus.OK;
            }
        });
        testRest.getRestTemplate().setMessageConverters(Arrays.asList(new MappingJackson2HttpMessageConverter(mapper)));
    }

    @Test
    public void saveNewTriggerAndFetchIt() {
        TriggerDefinition triggerDefinition = cron().identifiedAs(new NameAndGroup("name", "group"))
                                                    .forJob(new NameAndGroup("heartbeat", "builtin"))
                                                    .withData("key1", "value")
                                                    .withData("key2", 5)
                                                    .withCronExpression("0 0/1 * 1/1 * ? *")
                                                    .withMisfireInstruction(Cron.MisfireInstruction.FIRE_ONCE)
                                                    .build();
        testRest.postForObject("http://localhost:" + serverPort + "/triggers/new", triggerDefinition, Void.class);
        TriggerDefinition[] triggers = testRest.getForObject("http://localhost:" + serverPort + "/triggers/all", TriggerDefinition[].class);
        assertEquals(1, triggers.length);
        assertEquals("name", triggers[0].getTriggerKey().getName());
    }
}
