package com.changgou.user.dao;
import com.changgou.user.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:shenkunlin
 * @Description:User的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface UserMapper extends Mapper<User> {

    /**
     * 下单成功后用户积分增加
     */
    @Update("update tb_user set points = points+#{points} where username = #{username}")
    int updatePoints(@Param("points") Integer points, @Param("username") String username);

}
