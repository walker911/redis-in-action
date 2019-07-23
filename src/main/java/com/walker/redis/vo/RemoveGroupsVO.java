package com.walker.redis.vo;

import lombok.Data;

import java.util.List;

/**
 * @author walker
 * @date 2019/7/23
 */
@Data
public class RemoveGroupsVO {

    private String articleId;

    private List<String> toRemove;
}
