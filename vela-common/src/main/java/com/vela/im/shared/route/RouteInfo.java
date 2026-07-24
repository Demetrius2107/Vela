package com.vela.im.shared.route;

import lombok.Data;

/**
 * <p>Title: RouteInfo</p>
 * <p>Description: 路由节点信息，包含目标服务器的 IP 和端口号。</p>
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
public final class RouteInfo {

    private String ip;
    private Integer port;

    public RouteInfo(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }
}
