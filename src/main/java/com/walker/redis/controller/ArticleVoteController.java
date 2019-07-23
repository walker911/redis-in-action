package com.walker.redis.controller;

import com.walker.redis.common.ProjectConstant;
import com.walker.redis.common.ResultResponse;
import com.walker.redis.vo.AddGroupsVO;
import com.walker.redis.vo.RemoveGroupsVO;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author walker
 * @date 2019/7/17
 */
@RestController
public class ArticleVoteController {

    private final RedisTemplate<String, Object> redisTemplate;

    public ArticleVoteController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/vote")
    public String vote(String user, String article) {
        long cutoff = System.currentTimeMillis() - ProjectConstant.ONE_WEEK_IN_MILLIS;
        Double deadline = redisTemplate.opsForZSet().score("time:", article);
        if (deadline == null || deadline < cutoff) {
            return "FAIL";
        }

        String articleId = article.split(":")[1];
        Long result = redisTemplate.opsForSet().add("voted:" + articleId, user);
        if (result == null || result == 0) {
            return "ALREADY VOTE";
        }
        redisTemplate.opsForZSet().incrementScore("score:", article, ProjectConstant.VOTE_SCORE);
        redisTemplate.opsForHash().increment(article, "votes", 1);

        return "SUCCESS";
    }

    @PostMapping("/post/article")
    public ResultResponse<String> postArticle(String user, String title, String link) {
        // 生成文章ID
        String articleId = String.valueOf(redisTemplate.opsForValue().increment("article:"));

        String voted = "voted:" + articleId;
        redisTemplate.opsForSet().add(voted, user);
        redisTemplate.expire(voted, ProjectConstant.ONE_WEEK_IN_MILLIS, TimeUnit.MILLISECONDS);

        long now = System.currentTimeMillis();
        String article = "article:" + articleId;
        Map<String, Object> articleParams = new HashMap<>();
        articleParams.put("title", title);
        articleParams.put("link", link);
        articleParams.put("poster", user);
        articleParams.put("time", now);
        articleParams.put("votes", 1);
        redisTemplate.opsForHash().putAll(article, articleParams);

        redisTemplate.opsForZSet().add("score:", article, now + ProjectConstant.VOTE_SCORE);
        redisTemplate.opsForZSet().add("time:", article, now);

        return ResultResponse.success(articleId);
    }

    @GetMapping("/get/articles")
    public ResultResponse<List<Map<Object, Object>>> getArticles(int page, @RequestParam(defaultValue = "score:") String order) {
        int start = (page - 1) * ProjectConstant.ARTICLES_PER_PAGE;
        int end = start + ProjectConstant.ARTICLES_PER_PAGE - 1;

        Set<Object> ids = redisTemplate.opsForZSet().reverseRange(order, start, end);
        List<Map<Object, Object>> items = new ArrayList<>();
        if (ids != null) {
            ids.forEach(id -> {
                Map<Object, Object> params = redisTemplate.opsForHash().entries(id.toString());
                params.put("id", id);
                items.add(params);
            });
        }

        return ResultResponse.success(items);
    }

    @GetMapping("/get/group/articles")
    public ResultResponse<List<Map<Object, Object>>> getGroupArticles(int page, String group, @RequestParam(defaultValue = "score:") String order) {
        String key = order + group;
        Boolean hasKey = redisTemplate.hasKey(key);
        if (Objects.nonNull(hasKey) && !hasKey) {
            // 根据评分或者发布时间，对群组文章进行排序
            List<String> otherKeys = new ArrayList<>();
            otherKeys.add(order);

            redisTemplate.opsForZSet().intersectAndStore("group:" + group, otherKeys, key, RedisZSetCommands.Aggregate.MAX);
            redisTemplate.expire(key, 60, TimeUnit.SECONDS);
        }
        return getArticles(page, key);
    }

    @PostMapping("/add/groups")
    public ResultResponse addGroups(@RequestBody AddGroupsVO addGroupsVO) {
        String article = "article:" + addGroupsVO.getArticleId();
        addGroupsVO.getToAdd().forEach(group -> redisTemplate.opsForSet().add("group:" + group, article));

        return ResultResponse.success();
    }

    @PostMapping("/remove/groups")
    public ResultResponse removeGroups(@RequestBody RemoveGroupsVO removeGroupsVO) {
        String article = "article:" + removeGroupsVO.getArticleId();
        removeGroupsVO.getToRemove().forEach(group -> redisTemplate.opsForSet().remove("group:" + group, article));

        return ResultResponse.success();
    }
}
