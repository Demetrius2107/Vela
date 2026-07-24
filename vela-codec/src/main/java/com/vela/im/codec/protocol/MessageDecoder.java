package com.vela.im.codec.protocol;

import com.vela.im.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * <p>Title: MessageDecoder</p>
 * <p>Description: TCP 消息解码器，Netty ByteToMessageDecoder 实现，将二进制流解码为 Message 协议对象。</p>
 * <p>私有协议头部格式（定长28字节）：</p>
 * <pre>
 *   [4B command][4B version][4B clientType][4B messageType]
 *   [4B appId][4B imeiLength][imei][4B bodyLength][body]
 * </pre>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2025-03-03
 * @updateTime 2026-07-24
 *
 * Copyright © 2026 wanqiu All rights reserved
 */
public class MessageDecoder extends ByteToMessageDecoder {

    /**
     * 从 TCP 二进制流中解码出一条完整的消息。
     * <p>检查可读字节是否满足 28 字节包头长度，满足后委派 {@link ByteBufToMessageUtils#decode(ByteBuf)}
     * 进行实际解码，解码成功后将 Message 对象加入输出列表。</p>
     *
     * @param ctx Netty 通道处理器上下文
     * @param in  Netty 接收缓冲区（ByteBuf）
     * @param out 解码结果输出列表，解码成功后将 Message 对象加入此列表
     */
    @Override
    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf in, List<Object> out) {
        // 最小可读长度：28字节 = 7个int字段 x 4字节
        if (in.readableBytes() < 28) {
            return;
        }

        Message message = ByteBufToMessageUtils.decode(in);
        if (message == null) {
            return;
        }

        out.add(message);
    }
}
