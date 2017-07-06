package com.taboola.cronyx.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.taboola.cronyx.Cron;
import com.taboola.cronyx.Immediate;
import com.taboola.cronyx.Immediate.MisfireInstruction;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.SchedulingService;
import com.taboola.cronyx.TriggerDefinition;
import com.taboola.cronyx.TriggerDefinitionBuilder;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class SchedulingServiceRestControllerTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new SimpleModule().addDeserializer(TriggerDefinition.class, new TriggerDefinitionJsonDeserializer()));
    private final SchedulingService mockSchedulingService = mock(SchedulingService.class);
    private final MockMvc mockMvc = standaloneSetup(new SchedulingServiceRestController(mockSchedulingService)).setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
    private TriggerDefinition cronTrigger = TriggerDefinitionBuilder.cron()
                                                                    .identifiedAs(new NameAndGroup("test", "group"))
                                                                    .forJob(new NameAndGroup("job", "jobGroup"))
                                                                    .withData("key1", "value")
                                                                    .withData("key2", 5)
                                                                    .withCronExpression("0 0/1 * 1/1 * ? *")
                                                                    .withMisfireInstruction(Cron.MisfireInstruction.FIRE_ONCE)
                                                                    .build();
    private TriggerDefinition immediateTrigger = TriggerDefinitionBuilder.immediate()
                                                                         .identifiedAs(new NameAndGroup("test", "group"))
                                                                         .forJob(new NameAndGroup("job", "jobGroup"))
                                                                         .withData("key1", "value")
                                                                         .withData("key2", 5)
                                                                         .withMisfireInstruction(MisfireInstruction.DROP)
                                                                         .build();

    @Test
    public void listAllTriggers() throws Exception {
        when(mockSchedulingService.getAllTriggers()).thenReturn(singletonList(cronTrigger));
        mockMvc.perform(get("/triggers/all"))
               .andExpect(status().isOk())
               .andExpect(result -> assertEquals("application/json;charset=UTF-8", result.getResponse().getContentType()))
               .andExpect(result -> {
                   String responseBody = result.getResponse().getContentAsString();
                   List<TriggerDefinition> triggers = Arrays.asList(mapper.readValue(responseBody, TriggerDefinition[].class));
                   assertEquals(1, triggers.size());
                   assertTriggers(cronTrigger, triggers.get(0));
               });
        verify(mockSchedulingService).getAllTriggers();
    }

    @Test
    public void listSpecificTrigger() throws Exception {
        when(mockSchedulingService.getTriggerByKey(any(NameAndGroup.class))).thenReturn(immediateTrigger);
        mockMvc.perform(get("/triggers?name=test&group=group"))
               .andExpect(status().isOk())
               .andExpect(result -> assertEquals("application/json;charset=UTF-8", result.getResponse().getContentType()))
               .andExpect(result -> {
                   String responseBody = result.getResponse().getContentAsString();
                   TriggerDefinition trigger = mapper.readValue(responseBody, TriggerDefinition.class);
                   assertTriggers(immediateTrigger, trigger);
               });
        verify(mockSchedulingService).getTriggerByKey(argThat(new ArgumentMatcher<NameAndGroup>() {
            @Override
            public boolean matches(Object o) {
                return (o instanceof NameAndGroup) &&
                        ((NameAndGroup) o).getName().equals("test") &&
                        ((NameAndGroup) o).getGroup().equals("group");
            }
        }));
    }

    @Test
    public void listTriggersOfSpecificGroup() throws Exception {
        when(mockSchedulingService.getTriggersOfGroup(anyString())).thenReturn(singletonList(immediateTrigger));
        mockMvc.perform(get("/triggers/group/testGroup"))
               .andExpect(status().isOk())
               .andExpect(result -> assertEquals("application/json;charset=UTF-8", result.getResponse().getContentType()))
               .andExpect(result -> {
                   String responseBody = result.getResponse().getContentAsString();
                   List<TriggerDefinition> triggers = Arrays.asList(mapper.readValue(responseBody, TriggerDefinition[].class));
                   assertEquals(1, triggers.size());
                   assertTriggers(immediateTrigger, triggers.get(0));
               });
        verify(mockSchedulingService).getTriggersOfGroup(eq("testGroup"));
    }

    @Test
    public void listAllTriggerGroups() throws Exception {
        mockMvc.perform(get("/triggers/groups"))
               .andExpect(result -> assertEquals("application/json;charset=UTF-8", result.getResponse().getContentType()))
               .andExpect(status().isOk());
        verify(mockSchedulingService).getTriggerGroupNames();
    }

    @Test
    public void listTriggersForSpecificJob() throws Exception {
        when(mockSchedulingService.getTriggersForJob(any(NameAndGroup.class))).thenReturn(singletonList(cronTrigger));
        mockMvc.perform(get("/triggers/job?name=job&group=jobGroup"))
               .andExpect(status().isOk())
               .andExpect(result -> assertEquals("application/json;charset=UTF-8", result.getResponse().getContentType()))
               .andExpect(result -> {
                   String responseBody = result.getResponse().getContentAsString();
                   List<TriggerDefinition> triggers = Arrays.asList(mapper.readValue(responseBody, TriggerDefinition[].class));
                   assertEquals(1, triggers.size());
                   assertTriggers(cronTrigger, triggers.get(0));
               });
        verify(mockSchedulingService).getTriggersForJob(argThat(new ArgumentMatcher<NameAndGroup>() {
            @Override
            public boolean matches(Object o) {
                return (o instanceof NameAndGroup) &&
                        ((NameAndGroup) o).getName().equals("job") &&
                        ((NameAndGroup) o).getGroup().equals("jobGroup");
            }
        }));
    }

    @Test
    public void deleteATrigger() throws Exception {
        mockMvc.perform(post("/triggers/delete?name=test&group=group"))
               .andExpect(status().isOk());
        verify(mockSchedulingService).removeTrigger(argThat(new ArgumentMatcher<NameAndGroup>() {
            @Override
            public boolean matches(Object o) {
                return (o instanceof NameAndGroup) &&
                        ((NameAndGroup) o).getName().equals("test") &&
                        ((NameAndGroup) o).getGroup().equals("group");
            }
        }));
    }

    @Test
    public void listLocallyExecutingTriggers() throws Exception {
        when(mockSchedulingService.getLocallyExecutingTriggers()).thenReturn(singletonList(immediateTrigger));
        mockMvc.perform(get("/triggers/local"))
               .andExpect(status().isOk())
               .andExpect(result -> assertEquals("application/json;charset=UTF-8", result.getResponse().getContentType()))
               .andExpect(result -> {
                   String responseBody = result.getResponse().getContentAsString();
                   List<TriggerDefinition> triggers = Arrays.asList(mapper.readValue(responseBody, TriggerDefinition[].class));
                   assertEquals(1, triggers.size());
                   assertTriggers(immediateTrigger, triggers.get(0));
               });
        verify(mockSchedulingService).getLocallyExecutingTriggers();
    }

    @Test
    public void pauseATrigger() throws Exception {
        mockMvc.perform(post("/triggers/pause?name=test&group=group"))
               .andExpect(status().isOk());
        verify(mockSchedulingService).pauseTrigger(argThat(new ArgumentMatcher<NameAndGroup>() {
            @Override
            public boolean matches(Object o) {
                return (o instanceof NameAndGroup) &&
                        ((NameAndGroup) o).getName().equals("test") &&
                        ((NameAndGroup) o).getGroup().equals("group");
            }
        }));
    }

    @Test
    public void pauseAnEntireTriggerGroup() throws Exception {
        mockMvc.perform(post("/triggers/pause/tgroup/triggerGroup"))
               .andExpect(status().isOk());
        verify(mockSchedulingService).pauseTriggerGroup(eq("triggerGroup"));
    }

    @Test
    public void pauseAllTriggersForSpecificJob() throws Exception {
        mockMvc.perform(post("/triggers/pause/job?name=job&group=jobGroup"))
               .andExpect(status().isOk());
        verify(mockSchedulingService).pauseJob(argThat(new ArgumentMatcher<NameAndGroup>() {
            @Override
            public boolean matches(Object o) {
                return (o instanceof NameAndGroup) &&
                        ((NameAndGroup) o).getName().equals("job") &&
                        ((NameAndGroup) o).getGroup().equals("jobGroup");
            }
        }));
    }

    @Test
    public void pauseAnEntireJobGroup() throws Exception {
        mockMvc.perform(post("/triggers/pause/jgroup/jobGroup"))
               .andExpect(status().isOk());
        verify(mockSchedulingService).pauseJobGroup(eq("jobGroup"));
    }

    @Test
    public void pauseAllTriggers() throws Exception {
        mockMvc.perform(post("/triggers/pause/all"))
               .andExpect(status().isOk());
        verify(mockSchedulingService).pauseAll();
    }

    @Test
    public void resumeATrigger() throws Exception {
        mockMvc.perform(post("/triggers/resume?name=test&group=group"))
               .andExpect(status().isOk());
        verify(mockSchedulingService).resumeTrigger(argThat(new ArgumentMatcher<NameAndGroup>() {
            @Override
            public boolean matches(Object o) {
                return (o instanceof NameAndGroup) &&
                        ((NameAndGroup) o).getName().equals("test") &&
                        ((NameAndGroup) o).getGroup().equals("group");
            }
        }));
    }

    @Test
    public void resumeAnEntireTriggerGroup() throws Exception {
        mockMvc.perform(post("/triggers/resume/tgroup/triggerGroup"))
               .andExpect(status().isOk());
        verify(mockSchedulingService).resumeTriggerGroup(eq("triggerGroup"));
    }

    @Test
    public void resumeAllTriggersForSpecificJob() throws Exception {
        mockMvc.perform(post("/triggers/resume/job?name=job&group=jobGroup"))
               .andExpect(status().isOk());
        verify(mockSchedulingService).resumeJob(argThat(new ArgumentMatcher<NameAndGroup>() {
            @Override
            public boolean matches(Object o) {
                return (o instanceof NameAndGroup) &&
                        ((NameAndGroup) o).getName().equals("job") &&
                        ((NameAndGroup) o).getGroup().equals("jobGroup");
            }
        }));
    }

    @Test
    public void resumeAnEntireJobGroup() throws Exception {
        mockMvc.perform(post("/triggers/resume/jgroup/jobGroup"))
               .andExpect(status().isOk());
        verify(mockSchedulingService).resumeJobGroup(eq("jobGroup"));
    }

    @Test
    public void resumeAllTriggers() throws Exception {
        mockMvc.perform(post("/triggers/resume/all"))
               .andExpect(status().isOk());
        verify(mockSchedulingService).resumeAll();
    }

    @Test
    public void triggerAJob() throws Exception {
        when(mockSchedulingService.triggerNow(any(NameAndGroup.class))).thenReturn(immediateTrigger);
        mockMvc.perform(post("/triggers/start?name=test&group=group"))
               .andExpect(status().isOk())
               .andExpect(result -> assertEquals("application/json;charset=UTF-8", result.getResponse().getContentType()))
               .andExpect(result -> {
                   String responseBody = result.getResponse().getContentAsString();
                   TriggerDefinition trigger = mapper.readValue(responseBody, TriggerDefinition.class);
                   assertTriggers(immediateTrigger, trigger);
               });
        verify(mockSchedulingService).triggerNow(argThat(new ArgumentMatcher<NameAndGroup>() {
            @Override
            public boolean matches(Object o) {
                return (o instanceof NameAndGroup) &&
                        ((NameAndGroup) o).getName().equals("test") &&
                        ((NameAndGroup) o).getGroup().equals("group");
            }
        }));
    }

    @Test
    public void triggerANewImmediateTrigger() throws Exception {
        mockMvc.perform(post("/triggers/new/immediate").content(mapper.writeValueAsString(immediateTrigger)).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
        verify(mockSchedulingService).saveOrUpdateTrigger(argThat(new ArgumentMatcher<TriggerDefinition>() {
            @Override
            public boolean matches(Object o) {
                return (o instanceof Immediate) &&
                        ((Immediate) o).getTriggerKey().getName().equals("test") &&
                        ((Immediate) o).getTriggerKey().getGroup().equals("group") &&
                        ((Immediate) o).getJobKey().getName().equals("job") &&
                        ((Immediate) o).getJobKey().getGroup().equals("jobGroup") &&
                        ((Immediate) o).getTriggerData().get("key1").equals("value") &&
                        ((Immediate) o).getTriggerData().get("key2").equals(5) &&
                        ((Immediate) o).getMisfireInstruction() == MisfireInstruction.DROP;
            }
        }));
    }

    @Test
    public void createImmediateTrigger() throws Exception {
        mockMvc.perform(post("/triggers/new").content(mapper.writeValueAsString(immediateTrigger)).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
        verify(mockSchedulingService).saveOrUpdateTrigger(argThat(new ArgumentMatcher<TriggerDefinition>() {
            @Override
            public boolean matches(Object o) {
                return (o instanceof Immediate) &&
                        ((Immediate) o).getTriggerKey().getName().equals("test") &&
                        ((Immediate) o).getTriggerKey().getGroup().equals("group") &&
                        ((Immediate) o).getJobKey().getName().equals("job") &&
                        ((Immediate) o).getJobKey().getGroup().equals("jobGroup") &&
                        ((Immediate) o).getTriggerData().get("key1").equals("value") &&
                        ((Immediate) o).getTriggerData().get("key2").equals(5) &&
                        ((Immediate) o).getMisfireInstruction() == MisfireInstruction.DROP;
            }
        }));
    }

    @Test
    public void createCronTrigger() throws Exception {
        mockMvc.perform(post("/triggers/new").content(mapper.writeValueAsString(cronTrigger)).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
        verify(mockSchedulingService).saveOrUpdateTrigger(argThat(new ArgumentMatcher<TriggerDefinition>() {
            @Override
            public boolean matches(Object o) {
                return (o instanceof Cron) &&
                        ((Cron) o).getTriggerKey().getName().equals("test") &&
                        ((Cron) o).getTriggerKey().getGroup().equals("group") &&
                        ((Cron) o).getJobKey().getName().equals("job") &&
                        ((Cron) o).getJobKey().getGroup().equals("jobGroup") &&
                        ((Cron) o).getTriggerData().get("key1").equals("value") &&
                        ((Cron) o).getTriggerData().get("key2").equals(5) &&
                        ((Cron) o).getCronExpression().equals("0 0/1 * 1/1 * ? *") &&
                        ((Cron) o).getMisfireInstruction() == Cron.MisfireInstruction.FIRE_ONCE;
            }
        }));
    }

    private void assertTriggers(TriggerDefinition expectedTrigger, TriggerDefinition actualTrigger) {
        assertEquals(expectedTrigger.getClass(), actualTrigger.getClass());
        assertEquals(expectedTrigger.getTriggerKey().getName(), actualTrigger.getTriggerKey().getName());
        assertEquals(expectedTrigger.getTriggerKey().getGroup(), actualTrigger.getTriggerKey().getGroup());
        assertEquals(expectedTrigger.getJobKey().getName(), actualTrigger.getJobKey().getName());
        assertEquals(expectedTrigger.getJobKey().getGroup(), actualTrigger.getJobKey().getGroup());
        assertEquals(expectedTrigger.getTriggerData(), actualTrigger.getTriggerData());
        assertEquals(expectedTrigger.getDescription(), actualTrigger.getDescription());
        if (expectedTrigger instanceof Cron) {
            assertEquals(((Cron) expectedTrigger).getCronExpression(), ((Cron) actualTrigger).getCronExpression());
            assertEquals(((Cron) expectedTrigger).getMisfireInstruction(), ((Cron) actualTrigger).getMisfireInstruction());
        } else {
            assertEquals(((Immediate) expectedTrigger).getMisfireInstruction(), ((Immediate) actualTrigger).getMisfireInstruction());
        }
    }
}
