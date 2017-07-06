package com.taboola.cronyx.rest;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taboola.cronyx.ArgumentDefinition;
import com.taboola.cronyx.JobDefinition;
import com.taboola.cronyx.JobType;
import com.taboola.cronyx.JobsService;
import com.taboola.cronyx.NameAndGroup;

public class JobServiceRestControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JobsService mockSchedulingService = mock(JobsService.class);
    private final MockMvc mockMvc = standaloneSetup(new JobServiceRestController(mockSchedulingService))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
    private JobDefinition jobDefinition = new JobDefinition(new NameAndGroup("job", "jobGroup"), "description",
            new Object().getClass(), JobType.BUILTIN, new ArgumentDefinition[] {}, "methodname", false, false, false);

    @Before
    public void init() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void listAllJobs() throws Exception {
        when(mockSchedulingService.getAllJobs()).thenReturn(singletonList(jobDefinition));
        mockMvc.perform(get("/jobs/all"))
               .andExpect(status().isOk())
               .andExpect(result -> assertEquals("application/json;charset=UTF-8", result.getResponse()
                       .getContentType()))
               .andExpect(result -> {
                   String responseBody = result.getResponse().getContentAsString();
                   List<JobDefinition> jobs = Arrays.asList(mapper.readValue(responseBody, JobDefinition[].class));
                   assertEquals(1, jobs.size());
                   assertJobs(jobDefinition, jobs.get(0));
               });
        verify(mockSchedulingService).getAllJobs();
    }

    @Test
    public void listSpecificJob() throws Exception {
        when(mockSchedulingService.getJobByKey(any(NameAndGroup.class))).thenReturn(jobDefinition);
        mockMvc.perform(get("/jobs/?name=test&group=group"))
               .andExpect(status().isOk())
               .andExpect(result -> assertEquals("application/json;charset=UTF-8", result.getResponse()
                       .getContentType()))
               .andExpect(result -> {
                   String responseBody = result.getResponse().getContentAsString();
                   JobDefinition job = mapper.readValue(responseBody, JobDefinition.class);
                   assertJobs(jobDefinition, job);
               });
        verify(mockSchedulingService).getJobByKey(argThat(new ArgumentMatcher<NameAndGroup>() {
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
        when(mockSchedulingService.getJobsOfGroup(anyString())).thenReturn(singletonList(jobDefinition));
        mockMvc.perform(get("/jobs/group/testGroup"))
               .andExpect(status().isOk())
               .andExpect(result -> assertEquals("application/json;charset=UTF-8", result.getResponse()
                       .getContentType()))
               .andExpect(result -> {
                   String responseBody = result.getResponse().getContentAsString();
                   List<JobDefinition> jobs = Arrays.asList(mapper.readValue(responseBody, JobDefinition[].class));
                   assertEquals(1, jobs.size());
                   assertJobs(jobDefinition, jobs.get(0));
               });
        verify(mockSchedulingService).getJobsOfGroup(eq("testGroup"));
    }

    @Test
    public void listAllTriggerGroups() throws Exception {
        mockMvc.perform(get("/jobs/groups"))
               .andExpect(result -> assertEquals("application/json;charset=UTF-8", result.getResponse()
                       .getContentType()))
               .andExpect(status().isOk());
        verify(mockSchedulingService).getJobGroupNames();
    }

    private void assertJobs(JobDefinition expectedJob, JobDefinition actualJob) {
        assertEquals(expectedJob.getClass(), actualJob.getClass());
        assertEquals(expectedJob.getKey().getName(), actualJob.getKey().getName());
        assertEquals(expectedJob.getKey().getGroup(), actualJob.getKey().getGroup());
        assertEquals(expectedJob.getImplementingClass(), actualJob.getImplementingClass());
        assertEquals(expectedJob.getDescription(), actualJob.getDescription());
        assertEquals(expectedJob.getMethodName(), actualJob.getMethodName());
        assertEquals(expectedJob.getJobType(), actualJob.getJobType());
        assertTrue(Arrays.deepEquals(expectedJob.getArgs(), actualJob.getArgs()));
    }

}