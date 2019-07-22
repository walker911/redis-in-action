package com.walker.redis.controller;

import com.walker.redis.common.ProjectConstant;
import com.walker.redis.common.ResultResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/add/groups")
    public ResultResponse addGroups(String articleId, List<String> toAdd) {
        String article = "article:" + articleId;
        toAdd.forEach(group -> redisTemplate.opsForSet().add("group:" + group, article));

        return ResultResponse.success();
    }

    @PostMapping("/remove/groups")
    public ResultResponse removeGroups(String articleId, List<String> toRemove) {
        String article = "article:" + articleId;
        toRemove.forEach(group -> redisTemplate.opsForSet().remove("group:" + group, article));

        return ResultResponse.success();
    }
}
