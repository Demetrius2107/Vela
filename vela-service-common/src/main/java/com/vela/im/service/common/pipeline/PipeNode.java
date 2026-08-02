package com.vela.im.service.common.pipeline;

/**
 * <p>Title: PipeNode</p>
 * <p>Description: 管道节点接口，每个节点负责消息处理中的一个独立步骤。</p>
 * <p>节点通过调用 {@link PipeChain#next(Object)} 将控制权交给下一个节点，
 * 也可以选择不调用 next 来中断管道（如校验失败时返回错误）。</p>
 * <p>项目名称: Vela</p>
 *
 * @param <T> 上下文类型
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-24
 */
@FunctionalInterface
public interface PipeNode<T> {

    /**
     * 执行当前节点的处理逻辑。
     *
     * @param ctx   管道上下文，携带当前处理状态和数据
     * @param chain 管道链，用于将控制权传递给下一节点
     */
    void process(T ctx, PipeChain<T> chain);
}
