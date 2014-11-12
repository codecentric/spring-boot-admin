package de.codecentric.boot.admin.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Robert Winkler
 */
@ConfigurationProperties(prefix="cache")
public class CacheProperties {

    private String clusterName;

    @NotNull
    @Size(min = 1)
    private String clusterPassword;

    @NotNull
    private int clusterPort = 5701;

    @NotNull
    private boolean autoIncrementPort = true;

    private int portCount = 100;

    @NotNull
    private boolean multicastEnabled;

    private String multicastIp = "224.2.2.3";

    private int multicastPort = 54327;

    @NotNull
    private boolean tcpipEnabled;

    private String tcpipMembers = "127.0.0.1";

    private boolean interfaceEnabled;

    private String interfaceIp;

    private int timeToLiveSeconds = 0;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterPassword() {
        return clusterPassword;
    }

    public void setClusterPassword(String clusterPassword) {
        this.clusterPassword = clusterPassword;
    }

    public boolean isMulticastEnabled() {
        return multicastEnabled;
    }

    public void setMulticastEnabled(boolean multicastEnabled) {
        this.multicastEnabled = multicastEnabled;
    }

    public String getMulticastIp() {
        return multicastIp;
    }

    public void setMulticastIp(String multicastIp) {
        this.multicastIp = multicastIp;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public void setMulticastPort(int multicastPort) {
        this.multicastPort = multicastPort;
    }

    public boolean isTcpipEnabled() {
        return tcpipEnabled;
    }

    public void setTcpipEnabled(boolean tcpipEnabled) {
        this.tcpipEnabled = tcpipEnabled;
    }

    public String getTcpipMembers() {
        return tcpipMembers;
    }

    public void setTcpipMembers(String tcpipMembers) {
        this.tcpipMembers = tcpipMembers;
    }

    public boolean isInterfaceEnabled() {
        return interfaceEnabled;
    }

    public void setInterfaceEnabled(boolean interfaceEnabled) {
        this.interfaceEnabled = interfaceEnabled;
    }

    public String getInterfaceIp() {
        return interfaceIp;
    }

    public void setInterfaceIp(String interfaceIp) {
        this.interfaceIp = interfaceIp;
    }

    public boolean isAutoIncrementPort() {
        return autoIncrementPort;
    }

    public void setAutoIncrementPort(boolean autoIncrementPort) {
        this.autoIncrementPort = autoIncrementPort;
    }

    public int getClusterPort() {
        return clusterPort;
    }

    public void setClusterPort(int clusterPort) {
        this.clusterPort = clusterPort;
    }

    public int getPortCount() {
        return portCount;
    }

    public void setPortCount(int portCount) {
        this.portCount = portCount;
    }

    public int getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(int timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }
}
