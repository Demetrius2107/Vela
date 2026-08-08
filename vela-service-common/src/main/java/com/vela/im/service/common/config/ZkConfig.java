package com.vela.im.service.common.config;

import com.vela.im.shared.config.ImServerProperties;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * <p>Title: ZkConfig</p>
 * <p>Description: ZooKeeper 客户端配置</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 */
@Configuration
public class ZkConfig {

    @Bean
    @Lazy
    public ZkClient zkClient(ImServerProperties imServerProperties) {
        return new ZkClient(
                imServerProperties.getZookeeperAddr(),
                imServerProperties.getZookeeperConnectTimeOut() != null
                        ? imServerProperties.getZookeeperConnectTimeOut() : 5000
        );
    }
}
