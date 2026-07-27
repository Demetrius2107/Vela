package com.vela.im.service.message.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vela.im.service.message.domain.entity.ImStickerEntity;
import com.vela.im.service.message.domain.entity.ImStickerPackEntity;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImStickerMapper;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImStickerPackMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Title: StickerService</p>
 * <p>Description: 贴纸服务，提供贴纸包和贴纸的 CRUD 操作。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Service
public class StickerService {

    private final ImStickerPackMapper packMapper;
    private final ImStickerMapper stickerMapper;

    public StickerService(ImStickerPackMapper packMapper, ImStickerMapper stickerMapper) {
        this.packMapper = packMapper;
        this.stickerMapper = stickerMapper;
    }

    // ====== 贴纸包 ======

    public Result<List<ImStickerPackEntity>> listPacks(Integer appId) {
        QueryWrapper<ImStickerPackEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId).orderByAsc("sort_order");
        return Result.ok(packMapper.selectList(query));
    }

    public Result<ImStickerPackEntity> createPack(Integer appId, String name, String icon) {
        ImStickerPackEntity pack = new ImStickerPackEntity();
        pack.setAppId(appId);
        pack.setName(name);
        pack.setIcon(icon);
        pack.setSystemFlag(0);
        pack.setCreateTime(System.currentTimeMillis());
        pack.setUpdateTime(pack.getCreateTime());
        packMapper.insert(pack);
        return Result.ok(pack);
    }

    public Result<Void> deletePack(Long packId) {
        packMapper.deleteById(packId);
        // 级联删除该包下的所有贴纸
        stickerMapper.delete(new QueryWrapper<ImStickerEntity>().eq("pack_id", packId));
        return Result.ok();
    }

    // ====== 贴纸 ======

    public Result<List<ImStickerEntity>> listByPack(Long packId) {
        QueryWrapper<ImStickerEntity> query = new QueryWrapper<>();
        query.eq("pack_id", packId).orderByAsc("sort_order");
        return Result.ok(stickerMapper.selectList(query));
    }

    public Result<ImStickerEntity> create(Long packId, String name, String fileUrl,
                                          Integer width, Integer height) {
        ImStickerEntity sticker = new ImStickerEntity();
        sticker.setPackId(packId);
        sticker.setName(name);
        sticker.setFileUrl(fileUrl);
        sticker.setWidth(width);
        sticker.setHeight(height);
        sticker.setCreateTime(System.currentTimeMillis());
        stickerMapper.insert(sticker);
        return Result.ok(sticker);
    }

    public Result<Void> delete(Long stickerId) {
        stickerMapper.deleteById(stickerId);
        return Result.ok();
    }
}
