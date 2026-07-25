package com.vela.im.service.application.pipeline;

import java.util.List;

/**
 * <p>Title: PipeChain</p>
 * <p>Description: 管道链，按顺序执行一组 {@link PipeNode} 节点。</p>
 * <p>每个节点执行完毕后通过 {@link #next(Object)} 将控制权传递给下一节点。
 * 若节点不调用 next，则管道在此中断（终止处理）。</p>
 * <p>项目名称: Vela</p>
 *
 * @param <T> 上下文类型
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-24
 */
public class PipeChain<T> {

    private final List<PipeNode<T>> nodes;
    private int index;

    public PipeChain(List<PipeNode<T>> nodes) {
        this.nodes = nodes;
        this.index = 0;
    }

    /**
     * 启动管道处理。
     *
     * @param ctx 管道上下文
     */
    public void process(T ctx) {
        if (nodes.isEmpty()) {
            return;
        }
        index = 0;
        next(ctx);
    }

    /**
     * 将控制权传递给下一个节点。
     * <p>节点处理完毕后调用此方法来继续管道流程。
     * 如果已经是最后一个节点，则管道正常结束。</p>
     *
     * @param ctx 管道上下文
     */
    public void next(T ctx) {
        if (index >= nodes.size()) {
            return;
        }
        PipeNode<T> node = nodes.get(index++);
        node.process(ctx, this);
    }

    /**
     * 返回当前管道中剩余的节点数。
     */
    public int remaining() {
        return nodes.size() - index;
    }
}
