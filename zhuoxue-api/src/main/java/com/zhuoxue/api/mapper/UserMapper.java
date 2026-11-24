package com.zhuoxue.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuoxue.api.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表的数据访问接口（Mapper 层，也叫 DAO 层）。
 *
 * <p>这一层专门负责和数据库打交道。因为继承了 MyBatis-Plus 的 BaseMapper<User>，
 * 所以不用写任何 SQL，就自动拥有了常用的数据库操作方法，例如：</p>
 * <ul>
 *     <li>insert(user)：新增一条记录</li>
 *     <li>selectById(id)：按主键查询</li>
 *     <li>selectOne(wrapper)：按条件查询一条</li>
 *     <li>updateById(user)：按主键更新</li>
 *     <li>deleteById(id)：按主键删除</li>
 * </ul>
 *
 * <p>@Mapper 注解告诉 MyBatis：这是一个 Mapper 接口，启动时请自动生成它的实现类。</p>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
