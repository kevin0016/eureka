/*
 * Copyright 2012 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.netflix.appinfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import com.netflix.discovery.CommonConstants;
import com.netflix.discovery.shared.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract instance info configuration with some defaults to get the users
 * started quickly.The users have to override only a few methods to register
 * their instance with eureka server.
 *
 * @author Karthik Ranganathan
 *
 */
public abstract class AbstractInstanceConfig implements EurekaInstanceConfig {
    private static final Logger logger = LoggerFactory.getLogger(AbstractInstanceConfig.class);

    /**
     * @deprecated 2016-08-29 use {@link CommonConstants#DEFAULT_CONFIG_NAMESPACE}
     */
    @Deprecated
    public static final String DEFAULT_NAMESPACE = CommonConstants.DEFAULT_CONFIG_NAMESPACE;
    /**
     * 契约过期时间，单位：秒
     */
    private static final int LEASE_EXPIRATION_DURATION_SECONDS = 90;
    /**
     * 租约续约频率，单位：秒。
     */
    private static final int LEASE_RENEWAL_INTERVAL_SECONDS = 30;
    /**
     * 应用 https 端口关闭
     */
    private static final boolean SECURE_PORT_ENABLED = false;
    /**
     * 应用 http 端口开启
     */
    private static final boolean NON_SECURE_PORT_ENABLED = true;
    /**
     * 应用 http 端口
     */
    private static final int NON_SECURE_PORT = 80;
    /**
     * 应用 https 端口
     */
    private static final int SECURE_PORT = 443;
    /**
     * 应用初始化后开启
     */
    private static final boolean INSTANCE_ENABLED_ON_INIT = false;
    /**
     *  主机信息
     *  key：主机 IP 地址
     *  value：主机名
     *  {@link Pair 可以认为是一个KV的存储结构}
     */
    private static final Pair<String, String> hostInfo = getHostInfo();
    /**
     * 数据中心信息
     */
    private DataCenterInfo info = new DataCenterInfo() {
        @Override
        public Name getName() {
            return Name.MyOwn;
        }
    };

    protected AbstractInstanceConfig() {

    }

    protected AbstractInstanceConfig(DataCenterInfo info) {
        this.info = info;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#isInstanceEnabledOnit()
     */
    @Override
    public boolean isInstanceEnabledOnit() {
        return INSTANCE_ENABLED_ON_INIT;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getNonSecurePort()
     */
    @Override
    public int getNonSecurePort() {
        return NON_SECURE_PORT;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getSecurePort()
     */
    @Override
    public int getSecurePort() {
        return SECURE_PORT;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#isNonSecurePortEnabled()
     */
    @Override
    public boolean isNonSecurePortEnabled() {
        return NON_SECURE_PORT_ENABLED;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getSecurePortEnabled()
     */
    @Override
    public boolean getSecurePortEnabled() {
        // TODO Auto-generated method stub
        return SECURE_PORT_ENABLED;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.netflix.appinfo.InstanceConfig#getLeaseRenewalIntervalInSeconds()
     */
    @Override
    public int getLeaseRenewalIntervalInSeconds() {
        return LEASE_RENEWAL_INTERVAL_SECONDS;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.netflix.appinfo.InstanceConfig#getLeaseExpirationDurationInSeconds()
     */
    @Override
    public int getLeaseExpirationDurationInSeconds() {
        return LEASE_EXPIRATION_DURATION_SECONDS;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getVirtualHostName()
     */
    @Override
    public String getVirtualHostName() {
        return (getHostName(false) + ":" + getNonSecurePort());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getSecureVirtualHostName()
     */
    @Override
    public String getSecureVirtualHostName() {
        return (getHostName(false) + ":" + getSecurePort());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getASGName()
     */
    @Override
    public String getASGName() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getHostName()
     */
    @Override
    public String getHostName(boolean refresh) {
        return hostInfo.second();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getMetadataMap()
     */
    @Override
    public Map<String, String> getMetadataMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getDataCenterInfo()
     */
    @Override
    public DataCenterInfo getDataCenterInfo() {
        // TODO Auto-generated method stub
        return info;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getIpAddress()
     */
    @Override
    public String getIpAddress() {
        return hostInfo.first();
    }

    /**
     * 获取本地服务器的主机名和主机 IP 地址
     * =====此处有坑，如果主机有有多网卡或者虚拟机网卡，获取可能会错误=====
     * 解决方案：
     *  1、手动配置本机的 hostname + etc/hosts 文件，从而映射主机名和 IP 地址。
     *  2、使用spring-cloud-eureka-client 如果配置了eureka.instance.prefer-ip-address = true，spring会获取第一个非循环IP
     *      如果Java无法确定主机名，则将IP地址发送给Eureka。只有设置主机名的明确方法是设置eureka.instance.hostname属性。
     *      可以使用环境变量在运行时设置主机名 - 例如，eureka.instance.hostname=${HOST_NAME}。
     *
     * @link https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.1.2.RELEASE/single/spring-cloud-netflix.html#spring-cloud-eureka-server-prefer-ip-address
     *
     * @return  Pair<String, String>
     */
    private static Pair<String, String> getHostInfo() {
        Pair<String, String> pair;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            pair = new Pair<String, String>(localHost.getHostAddress(), localHost.getHostName());
        } catch (UnknownHostException e) {
            logger.error("Cannot get host info", e);
            pair = new Pair<String, String>("", "");
        }
        return pair;
    }

}
