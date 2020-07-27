package com.changgou.user.feign;

import com.changgou.user.pojo.User;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 20:32 2019/8/24
 */
@FeignClient(name = "user")
@RequestMapping("/user")
public interface UserFeign {


    /**
     * @param points
     * @return entity.Result
     * @author 栗子
     * @Description 给用户添加积分
     * @Date 23:44 2019/8/26
     **/
    @GetMapping(value = "/points/{points}/{username}")
    Result addPoints(@PathVariable(value = "points") Integer points, @PathVariable(value = "username") String username);

    /**
     * 根据用户名查询查询用户
     */
    @GetMapping("/{id}")
    Result<User> findById(@PathVariable(value = "id") String id);

}
