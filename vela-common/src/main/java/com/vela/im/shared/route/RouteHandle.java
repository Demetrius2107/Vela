package com.vela.im.shared.route;

import java.util.List;

/**
 * <p>Title: RouteHandle</p>
 * <p>Description: 路由策略接口，定义从服务器列表中按路由策略选取目标服务器的行为。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2025-03-03
 * @updateTime 2026-07-24
 *
 * Copyright © 2026 wanqiu All rights reserved
 */
public interface RouteHandle {

    /**
     * 路由策略获取服务器地址
     * @param values
     * @param key
     * @return
     */
    public String routeServer(List<String> values,String key);

}
