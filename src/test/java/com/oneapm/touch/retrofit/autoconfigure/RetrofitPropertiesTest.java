package com.oneapm.touch.retrofit.autoconfigure;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RetrofitPropertiesTest {

    @Autowired
    private RetrofitProperties properties;

    @Test
    public void listInjectionFromConfigurationFile() throws Exception {
        assertThat(properties, notNullValue());
    }

    @Test
    public void injectedValueShouldBeTheSameAsTheConfigurationFile() throws Exception {
        assertThat(properties.getEndpoints(), hasSize(4));
        RetrofitProperties.Connection connection = properties.getConnection();
        assertThat(connection.getTimeout(), is(5000L));
        assertThat(connection.getKeepAliveDuration(), is(5));
        assertThat(connection.getMaxIdleConnections(), is(5));
    }
}
