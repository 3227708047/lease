package com.atguigu.lease.web.admin.mapper;

import com.atguigu.lease.model.entity.RoomAttrValue;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author liubo
* @description 针对表【room_attr_value(房间&基本属性值关联表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.atguigu.lease.model.RoomAttrValue
*/
public interface RoomAttrValueMapper extends BaseMapper<RoomAttrValue> {

    List<AttrValueVo> selectByRoomId(Long id);
}




