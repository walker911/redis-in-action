package com.walker.redis.model;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author mu qin
 * @date 2020/1/7
 */
@Data
public class User implements Serializable {

    private String uid;
    private String name;

}
