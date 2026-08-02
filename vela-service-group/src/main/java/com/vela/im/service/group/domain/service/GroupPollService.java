package com.vela.im.service.group.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vela.im.service.group.domain.entity.ImGroupPollEntity;
import com.vela.im.service.group.domain.entity.ImGroupPollOptionEntity;
import com.vela.im.service.group.domain.entity.ImGroupPollVoteEntity;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupPollMapper;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupPollOptionMapper;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupPollVoteMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroupPollService {

    private final ImGroupPollMapper pollMapper;
    private final ImGroupPollOptionMapper optionMapper;
    private final ImGroupPollVoteMapper voteMapper;

    public GroupPollService(ImGroupPollMapper pollMapper,
                            ImGroupPollOptionMapper optionMapper,
                            ImGroupPollVoteMapper voteMapper) {
        this.pollMapper = pollMapper;
        this.optionMapper = optionMapper;
        this.voteMapper = voteMapper;
    }

    @Transactional
    public Result<ImGroupPollEntity> createPoll(Integer appId, String groupId, String title,
                                                 String creatorId, Integer multipleChoice,
                                                 Long endTime, List<String> options) {
        if (options == null || options.size() < 2) {
            return Result.fail(400, "至少需要两个选项");
        }
        ImGroupPollEntity poll = new ImGroupPollEntity();
        poll.setAppId(appId);
        poll.setGroupId(groupId);
        poll.setTitle(title);
        poll.setCreatorId(creatorId);
        poll.setMultipleChoice(multipleChoice != null ? multipleChoice : 0);
        poll.setStatus(0);
        poll.setCreateTime(System.currentTimeMillis());
        poll.setEndTime(endTime);
        pollMapper.insert(poll);

        for (int i = 0; i < options.size(); i++) {
            ImGroupPollOptionEntity opt = new ImGroupPollOptionEntity();
            opt.setPollId(poll.getId());
            opt.setText(options.get(i));
            opt.setSortOrder(i);
            optionMapper.insert(opt);
        }
        return Result.ok(poll);
    }

    public Result<List<ImGroupPollEntity>> listPolls(String groupId, Integer appId) {
        QueryWrapper<ImGroupPollEntity> query = new QueryWrapper<>();
        query.eq("group_id", groupId).eq("app_id", appId).ne("status", 2).orderByDesc("create_time");
        return Result.ok(pollMapper.selectList(query));
    }

    @Transactional
    public Result<Void> vote(Long pollId, Long optionId, String voterId) {
        ImGroupPollEntity poll = pollMapper.selectById(pollId);
        if (poll == null || poll.getStatus() == 2) return Result.fail(400, "投票不存在");
        if (poll.getStatus() == 1) return Result.fail(400, "投票已结束");
        if (poll.getEndTime() != null && System.currentTimeMillis() > poll.getEndTime()) {
            poll.setStatus(1);
            pollMapper.updateById(poll);
            return Result.fail(400, "投票已结束");
        }

        QueryWrapper<ImGroupPollVoteEntity> check = new QueryWrapper<>();
        check.eq("poll_id", pollId).eq("voter_id", voterId);

        if (poll.getMultipleChoice() == 0) {
            if (voteMapper.selectCount(check) > 0) {
                return Result.fail(400, "已投过票");
            }
        }
        ImGroupPollVoteEntity vote = new ImGroupPollVoteEntity();
        vote.setPollId(pollId);
        vote.setOptionId(optionId);
        vote.setVoterId(voterId);
        vote.setVoteTime(System.currentTimeMillis());
        voteMapper.insert(vote);
        return Result.ok();
    }

    public Result<PollResult> getResult(Long pollId) {
        ImGroupPollEntity poll = pollMapper.selectById(pollId);
        if (poll == null) return Result.fail(400, "投票不存在");

        List<ImGroupPollOptionEntity> options = optionMapper.selectList(
                new QueryWrapper<ImGroupPollOptionEntity>().eq("poll_id", pollId));

        Map<Long, Integer> votes = new HashMap<>();
        for (ImGroupPollOptionEntity opt : options) {
            votes.put(opt.getId(), voteMapper.selectCount(
                    new QueryWrapper<ImGroupPollVoteEntity>().eq("option_id", opt.getId())));
        }
        return Result.ok(new PollResult(poll, options, votes));
    }

    @Transactional
    public Result<Void> closePoll(Long pollId) {
        ImGroupPollEntity poll = pollMapper.selectById(pollId);
        if (poll == null) return Result.fail(400, "投票不存在");
        poll.setStatus(1);
        pollMapper.updateById(poll);
        return Result.ok();
    }

    public static class PollResult {
        public ImGroupPollEntity poll;
        public List<ImGroupPollOptionEntity> options;
        public Map<Long, Integer> votes;
        public PollResult(ImGroupPollEntity poll, List<ImGroupPollOptionEntity> options, Map<Long, Integer> votes) {
            this.poll = poll; this.options = options; this.votes = votes;
        }
    }
    public static class PollResultVO {
        public ImGroupPollEntity poll;
        public List<OptionVO> optionList;
        public PollResultVO(ImGroupPollEntity poll, List<OptionVO> optionList) {
            this.poll = poll; this.optionList = optionList;
        }
    }
    public static class OptionVO {
        public ImGroupPollOptionEntity option;
        public int voteCount;
        public OptionVO(ImGroupPollOptionEntity option, int voteCount) {
            this.option = option; this.voteCount = voteCount;
        }
    }
}
