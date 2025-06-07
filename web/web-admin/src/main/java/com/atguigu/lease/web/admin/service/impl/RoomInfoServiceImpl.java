package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {
    @Autowired
    private GraphInfoService graphInfoService; //图片信息服务

    @Autowired
    private RoomAttrValueService roomAttrValueService; //属性值服务

    @Autowired
    private RoomFacilityService roomFacilityService;//配套服务

    @Autowired
    private RoomLabelService roomLabelService;//标签服务

    @Autowired
    private  RoomPaymentTypeService roomPaymentTypeService;//支付方式服务

    @Autowired
    private RoomLeaseTermService roomLeaseTermService;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private AttrValueMapper  attrValueMapper;

    @Autowired
    private FacilityInfoMapper facilityInfoMapper;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private PaymentTypeMapper   paymentTypeMapper;

    @Autowired
    private LeaseTermMapper leaseTermMapper;


    @Override
    public void saveOrUpdateApartment(RoomSubmitVo roomSubmitVo) {
        boolean isUpdate=roomSubmitVo.getId()!=null;
        super.saveOrUpdate(roomSubmitVo);
        if (isUpdate){
            // 1.删除图片列表
            LambdaQueryWrapper<GraphInfo> query = new LambdaQueryWrapper<>();
            query.eq(GraphInfo::getItemType, ItemType.ROOM);
            query.eq(GraphInfo::getItemId,roomSubmitVo.getId());
            graphInfoService.remove(query);

            //2.删除属性信息列表
            LambdaQueryWrapper<RoomAttrValue> RoomWrapper = new LambdaQueryWrapper<>();
            RoomWrapper.eq(RoomAttrValue::getRoomId,roomSubmitVo.getId());
            roomAttrValueService.remove(RoomWrapper);

            //3.删除配套信息列表
            LambdaQueryWrapper<RoomFacility> RoomFacilityWrapper = new LambdaQueryWrapper<>();
            RoomFacilityWrapper.eq(RoomFacility::getRoomId,roomSubmitVo.getId());
            roomFacilityService.remove(RoomFacilityWrapper);

            //4.删除标签信息列表
            LambdaQueryWrapper<RoomLabel> RoomLabelWrapper = new LambdaQueryWrapper<>();
            RoomLabelWrapper.eq(RoomLabel::getRoomId,roomSubmitVo.getId());
            roomLabelService.remove(RoomLabelWrapper);

            //5,删除支付方式列表
            LambdaQueryWrapper<RoomPaymentType> RoomPaymentTypeWrapper = new LambdaQueryWrapper<>();
            RoomPaymentTypeWrapper.eq(RoomPaymentType::getRoomId,roomSubmitVo.getId());
            roomPaymentTypeService.remove(RoomPaymentTypeWrapper);


            //6，删除可选租期列表
            LambdaQueryWrapper<RoomLeaseTerm> RoomLeaseTermWrapper = new LambdaQueryWrapper<>();
            RoomLeaseTermWrapper.eq(RoomLeaseTerm::getRoomId,roomSubmitVo.getId());
            roomLeaseTermService.remove(RoomLeaseTermWrapper);
        }
        // 1.插入图片列表
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        ArrayList<GraphInfo> graphVoList2 = new ArrayList<>();
        if(graphVoList!=null && !graphVoList.isEmpty()){
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfo.setName(graphInfo.getName());
                graphVoList2.add(graphInfo);
            }
            graphInfoService.saveBatch(graphVoList2);
        }

        //2.插入属性信息列表
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if(attrValueIds!=null && ! attrValueIds.isEmpty()){
            ArrayList<RoomAttrValue> roomAttrValues = new ArrayList<>();
            for (Long attrValueId : attrValueIds) {
                RoomAttrValue roomAttrValue = new RoomAttrValue();
                roomAttrValue.setRoomId(roomSubmitVo.getId());
                roomAttrValue.setAttrValueId(attrValueId);
                roomAttrValues.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValues);
        }

        //3.插入配套信息列表
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if(facilityInfoIds!=null && !facilityInfoIds.isEmpty()){
            ArrayList<RoomFacility> roomFacilities = new ArrayList<>();
            for (Long facilityInfoId : facilityInfoIds) {
                RoomFacility roomFacility = new RoomFacility();
                roomFacility.setRoomId(roomSubmitVo.getId());
                roomFacility.setFacilityId(facilityInfoId);
                roomFacilities.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilities);
        }

        //4.插入标签信息列表
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if(labelInfoIds!=null && !labelInfoIds.isEmpty()){
            ArrayList<RoomLabel> roomLabels = new ArrayList<>();
            for (Long labelInfoId : labelInfoIds) {
                RoomLabel roomLabel = new RoomLabel();
                roomLabel.setLabelId(labelInfoId);
                roomLabel.setRoomId(roomSubmitVo.getId());
                roomLabels.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabels);
        }

        //5,插入支付方式列表
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if(paymentTypeIds!=null && !paymentTypeIds.isEmpty()){
            ArrayList<RoomPaymentType> roomPaymentTypes = new ArrayList<>();
            for (Long paymentTypeId : paymentTypeIds) {
                RoomPaymentType roomPaymentType = new RoomPaymentType();
                roomPaymentType.setRoomId(roomSubmitVo.getId());
                roomPaymentType.setPaymentTypeId(paymentTypeId);
                roomPaymentTypes.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypes);
        }

        //6，插入可选租期列表
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if(leaseTermIds!=null && !leaseTermIds.isEmpty()){
            ArrayList<RoomLeaseTerm> roomLeaseTerms = new ArrayList<>();
            for (Long leaseTermId : leaseTermIds) {
                RoomLeaseTerm roomLeaseTerm = new RoomLeaseTerm();
                roomLeaseTerm.setRoomId(roomSubmitVo.getId());
                roomLeaseTerm.setLeaseTermId(leaseTermId);
                roomLeaseTerms.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }

    }

    @Override
    public IPage<RoomItemVo> pageItem(Page<RoomItemVo> page, RoomQueryVo queryVo) {
        return roomInfoMapper.pageItem(page,queryVo);
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        //1.查询RoomInfo
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        //2.查询所属公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());

        //3.查询graphInfoList
        List<GraphVo> graphVos = graphInfoMapper.selectListByItemIdAndItemType(id, ItemType.ROOM);
        //4.查询attrValueList
        List<AttrValueVo> attrValueVos=attrValueMapper.selectListByRoomId(id);
        //5.查询facilityInfoList
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(id);

        //6.查询labelInfoList
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

        //7.查询paymentTypeList
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);

        //8.查询leaseTermList
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);

        //6.组合结果
        RoomDetailVo roomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo, roomDetailVo);
        roomDetailVo.setApartmentInfo(apartmentInfo);
        roomDetailVo.setGraphVoList(graphVos);
        roomDetailVo.setAttrValueVoList(attrValueVos);
        roomDetailVo.setLabelInfoList(labelInfoList);
        roomDetailVo.setFacilityInfoList(facilityInfoList);
        roomDetailVo.setLeaseTermList(leaseTermList);
        roomDetailVo.setPaymentTypeList(paymentTypeList);

        return roomDetailVo;
    }

    @Override
    public void removeRoomById(Long id) {
        super.removeById(id);
        // 1.删除图片列表
        LambdaQueryWrapper<GraphInfo> query = new LambdaQueryWrapper<>();
        query.eq(GraphInfo::getItemType, ItemType.ROOM);
        query.eq(GraphInfo::getItemId,id);
        graphInfoService.remove(query);

        //2.删除属性信息列表
        LambdaQueryWrapper<RoomAttrValue> RoomWrapper = new LambdaQueryWrapper<>();
        RoomWrapper.eq(RoomAttrValue::getRoomId,id);
        roomAttrValueService.remove(RoomWrapper);

        //3.删除配套信息列表
        LambdaQueryWrapper<RoomFacility> RoomFacilityWrapper = new LambdaQueryWrapper<>();
        RoomFacilityWrapper.eq(RoomFacility::getRoomId,id);
        roomFacilityService.remove(RoomFacilityWrapper);

        //4.删除标签信息列表
        LambdaQueryWrapper<RoomLabel> RoomLabelWrapper = new LambdaQueryWrapper<>();
        RoomLabelWrapper.eq(RoomLabel::getRoomId,id);
        roomLabelService.remove(RoomLabelWrapper);

        //5,删除支付方式列表
        LambdaQueryWrapper<RoomPaymentType> RoomPaymentTypeWrapper = new LambdaQueryWrapper<>();
        RoomPaymentTypeWrapper.eq(RoomPaymentType::getRoomId,id);
        roomPaymentTypeService.remove(RoomPaymentTypeWrapper);


        //6，删除可选租期列表
        LambdaQueryWrapper<RoomLeaseTerm> RoomLeaseTermWrapper = new LambdaQueryWrapper<>();
        RoomLeaseTermWrapper.eq(RoomLeaseTerm::getRoomId,id);
        roomLeaseTermService.remove(RoomLeaseTermWrapper);

    }




}




