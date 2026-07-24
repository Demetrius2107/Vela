package com.vela.im.shared.config;

import lombok.Data;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>Title: GlobalHttpClientConfig</p>
 * <p>Description: HttpClient 连接池配置类，配置最大连接数、超时时间等参数。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2025-03-03
 * @updateTime 2026-07-24
 *
 * Copyright © 2026 wanqiu All rights reserved
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "httpclient")
public class GlobalHttpClientConfig {

    /** 最大连接数 */
    private Integer maxTotal;

    /** 最大并发链接数 */
    private Integer defaultMaxPerRoute;

    /** 创建链接的最大时间 */
    private Integer connectTimeout;

    /** 链接获取超时时间 */
    private Integer connectionRequestTimeout;

    /** 数据传输最长时间 */
    private Integer socketTimeout;

    /** 提交时检查链接是否可用 */
    private boolean staleConnectionCheckEnabled;

    /** HttpClient 连接池管理器 */
    private PoolingHttpClientConnectionManager manager;

    /** HttpClient 构建器 */
    private HttpClientBuilder httpClientBuilder;

    /**
     * 获取 HttpClient 连接池管理器（Bean）
     *
     * @return 连接池管理器实例
     */
    @Bean(name = "httpClientConnectionManager")
    public PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager() {
        return getManager();
    }

    /**
     * 获取或创建连接池管理器（单例模式）
     */
    private PoolingHttpClientConnectionManager getManager() {
        if (manager != null) {
            return manager;
        }
        manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(maxTotal);
        manager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        return manager;
    }

    /**
     * 实例化 HttpClientBuilder，注入连接池管理器
     *
     * @param httpClientConnectionManager 连接池管理器
     * @return HttpClientBuilder 实例
     */
    @Bean(name = "httpClientBuilder")
    public HttpClientBuilder getHttpClientBuilder(
            @Qualifier("httpClientConnectionManager") PoolingHttpClientConnectionManager httpClientConnectionManager) {
        httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(httpClientConnectionManager);
        return httpClientBuilder;
    }

    /**
     * 获取 CloseableHttpClient 实例（注入 builder）
     *
     * @param httpClientBuilder 已配置的 HttpClientBuilder
     * @return CloseableHttpClient 实例
     */
    @Bean
    public CloseableHttpClient getCloseableHttpClient(
            @Qualifier("httpClientBuilder") HttpClientBuilder httpClientBuilder) {
        return httpClientBuilder.build();
    }

    /**
     * 获取或创建 CloseableHttpClient 实例（无注入时使用默认配置）
     *
     * @return CloseableHttpClient 实例
     */
    public CloseableHttpClient getCloseableHttpClient() {
        if (httpClientBuilder != null) {
            return httpClientBuilder.build();
        }
        httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(getManager());
        return httpClientBuilder.build();
    }

    /**
     * 配置连接超时参数并构建 RequestConfig.Builder
     *
     * @return RequestConfig.Builder
     */
    @Bean(name = "builder")
    public RequestConfig.Builder getBuilder() {
        RequestConfig.Builder builder = RequestConfig.custom();
        return builder.setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setSocketTimeout(socketTimeout);
    }

    /**
     * 构建 RequestConfig 对象
     *
     * @param builder 已配置的 RequestConfig.Builder
     * @return RequestConfig 实例
     */
    @Bean
    public RequestConfig getRequestConfig(@Qualifier("builder") RequestConfig.Builder builder) {
        return builder.build();
    }
}
