package de.codecentric.boot.admin.cache.impl;

import com.hazelcast.config.*;
import com.hazelcast.instance.GroupProperties;
import de.codecentric.boot.admin.cache.CacheName;
import de.codecentric.boot.admin.cache.config.CacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author Robert Winkler
 */
public class HazelcastConfigFactory {

    private static Logger logger = LoggerFactory.getLogger(HazelcastConfigFactory.class);

    public static Config newHazelcastConfig(CacheProperties cacheProperties, Environment environment){
            Assert.notNull(cacheProperties,"cacheProperties must not be null" );
            Assert.notNull(environment,"environment must not be null" );
            String applicationName = environment.getProperty("spring.application.name");
            Assert.notNull(applicationName,"spring.application.name must not be null" );
            String hazelcastInstanceName = applicationName + "-hazelcast-instance";

            Config config = new Config(hazelcastInstanceName);
            config.setProperty(GroupProperties.PROP_ENABLE_JMX, "true");
            config.setProperty(GroupProperties.PROP_VERSION_CHECK_ENABLED , "false");

            GroupConfig groupConfig = config.getGroupConfig();
            configureGroupConfig(cacheProperties, applicationName, groupConfig);

            NetworkConfig network = config.getNetworkConfig();
            configureNetworkConfig(cacheProperties, network);

            Map<String, MapConfig> hazelcastMapConfigs = config.getMapConfigs();
            MapConfig registeredApplicationsMapConfig = new MapConfig();
            registeredApplicationsMapConfig.setName(CacheName.REGISTERED_APPLICATIONS.name());
            registeredApplicationsMapConfig.setTimeToLiveSeconds(cacheProperties.getTimeToLiveSeconds());
            hazelcastMapConfigs.put(CacheName.REGISTERED_APPLICATIONS.name(), registeredApplicationsMapConfig);

            logger.info("Created Hazelcast config for instance '" + hazelcastInstanceName + "'");
        return config;
    }

    private static void configureNetworkConfig(CacheProperties cacheProperties, NetworkConfig network) {
        network.setPort(cacheProperties.getClusterPort());
        network.setPortAutoIncrement(cacheProperties.isAutoIncrementPort());
        network.setPortCount(cacheProperties.getPortCount());

        JoinConfig join = network.getJoin();
        configureJoinConfig(cacheProperties, network, join);
    }

    private static void configureJoinConfig(CacheProperties cacheProperties, NetworkConfig network, JoinConfig join) {
        join.getAwsConfig().setEnabled(false);

        MulticastConfig multicastConfig = join.getMulticastConfig();
        configureMulticastConfig(cacheProperties, multicastConfig);

        TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
        configureTcpIpConfig(cacheProperties, tcpIpConfig);

        InterfacesConfig interfacesConfig = network.getInterfaces();
        configureInterfacesConfig(cacheProperties, interfacesConfig);
    }

    private static void configureMulticastConfig(CacheProperties cacheProperties, MulticastConfig multicastConfig) {
        multicastConfig.setEnabled(cacheProperties.isMulticastEnabled());
        if(multicastConfig.isEnabled()) {
            multicastConfig.setMulticastGroup(cacheProperties.getMulticastIp());
            multicastConfig.setMulticastPort(cacheProperties.getMulticastPort());
        }
    }

    private static void configureTcpIpConfig(CacheProperties cacheProperties, TcpIpConfig tcpIpConfig) {
        tcpIpConfig.setEnabled(cacheProperties.isTcpipEnabled());
        if(tcpIpConfig.isEnabled()) {
            tcpIpConfig.addMember(cacheProperties.getTcpipMembers());
        }
    }

    private static void configureInterfacesConfig(CacheProperties cacheProperties, InterfacesConfig interfacesConfig) {
        interfacesConfig.setEnabled(cacheProperties.isInterfaceEnabled());
        if (interfacesConfig.isEnabled()){
            interfacesConfig.addInterface(cacheProperties.getInterfaceIp());
        }
    }

    private static void configureGroupConfig(CacheProperties cacheProperties, String applicationName, GroupConfig groupConfig) {
        groupConfig.setName(getClusterName(cacheProperties, applicationName));
        groupConfig.setPassword(cacheProperties.getClusterPassword());
    }

    private static String getClusterName(CacheProperties cacheProperties, String applicationName) {
        return StringUtils.hasLength(cacheProperties.getClusterName()) ? cacheProperties.getClusterName() : applicationName + "-hazelcast-cluster";
    }
}
