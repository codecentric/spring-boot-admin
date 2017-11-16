package de.codecentric.boot.admin.notify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.*;

public class MicrosoftTeamsNotifierTest {


    private MicrosoftTeamsNotifier testMTNotifier;

    private RestTemplate mockRestTemplate;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test_onClientApplicationDeregisteredEvent_resolve() {

    }

    @Test
    public void test_onApplicationRegisteredEvent_resolve() {

    }

    @Test
    public void test_onApplicationStatusChangedEvent_resolve() {

    }

    @Test
    public void test_onRoutesOutdatedEvent_resolve() {

    }

}