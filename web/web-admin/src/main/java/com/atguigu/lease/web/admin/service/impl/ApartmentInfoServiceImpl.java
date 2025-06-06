package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private FacilityInfoMapper facilityInfoMapper;

    @Autowired
    private FeeValueMapper feeValueMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private GraphInfoService  graphInfoService;

    @Autowired
    private ApartmentFacilityService apartmentFacilityService;

    @Autowired
    private ApartmentLabelService  apartmentLabelService;

    @Autowired
    private ApartmentFeeValueService  apartmentFeeValueService;


    @Override
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        boolean isUpdate=apartmentSubmitVo.getId()!=null;
        super.saveOrUpdate(apartmentSubmitVo);
        if(isUpdate){
            // 1.删除图片列表
            LambdaQueryWrapper<GraphInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            queryWrapper.eq(GraphInfo::getItemId,apartmentSubmitVo.getId());
            graphInfoService.remove(queryWrapper);
            //2.删除配套列表
            LambdaQueryWrapper<ApartmentFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
            facilityQueryWrapper.eq(ApartmentFacility::getApartmentId,apartmentSubmitVo.getId());
            apartmentFacilityService.remove(facilityQueryWrapper);
            //3.删除标签列表
            LambdaQueryWrapper<ApartmentLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
            labelQueryWrapper.eq(ApartmentLabel::getApartmentId,apartmentSubmitVo.getId());
            apartmentLabelService.remove(labelQueryWrapper);

            //4.删除杂费列表
            LambdaQueryWrapper<ApartmentFeeValue> feeValueQueryWrapper = new LambdaQueryWrapper<>();
            feeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId,apartmentSubmitVo.getId());
            apartmentFeeValueService.remove(feeValueQueryWrapper);
        }
        // 1.插入图片列表
        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        if (graphVoList != null && !graphVoList.isEmpty()) {
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfo.setItemId(apartmentSubmitVo.getId());
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }

        //2.插入配套列表
        List<Long> facilityInfoIds = apartmentSubmitVo.getFacilityInfoIds();
        ArrayList<ApartmentFacility> facilityList = new ArrayList<>();
        if (facilityInfoIds != null && !facilityInfoIds.isEmpty()) {
            for (Long facilityInfoId : facilityInfoIds) {
                ApartmentFacility apartmentFacility = new ApartmentFacility();
                apartmentFacility.setFacilityId(facilityInfoId);
                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                facilityList.add(apartmentFacility);
            }
            apartmentLabelService.saveBatch(facilityList);
        }


        //3.插入标签列表

        List<Long> labelIdList = apartmentSubmitVo.getLabelIds();
        if(labelIdList!=null &&!labelIdList.isEmpty() ){
            ArrayList<ApartmentLabel> labelList = new ArrayList<>();
            for (Long labelId : labelIdList) {
                ApartmentLabel label = new ApartmentLabel();
                label.setLabelId(labelId);
                label.setApartmentId(apartmentSubmitVo.getId());
                labelList.add(label);
            }
            apartmentLabelService.saveBatch(labelList);
        }
        //4.插入杂费列表
        List<Long> feeValueIdList = apartmentSubmitVo.getFeeValueIds();
        if (feeValueIdList!=null && !feeValueIdList.isEmpty() ){
            ArrayList<ApartmentFeeValue> feeValueList = new ArrayList<>();
            for (Long feeValueId : feeValueIdList) {
                ApartmentFeeValue feeValue = new ApartmentFeeValue();
                feeValue.setApartmentId(apartmentSubmitVo.getId());
                feeValue.setFeeValueId(feeValueId);
                feeValueList.add(feeValue);
            }
            apartmentFeeValueService.saveBatch(feeValueList);
        }

    }

    @Override
    public IPage<ApartmentItemVo> pageItem(Page<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        return apartmentInfoMapper.pageItem(page,queryVo);
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //1.查询公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        //2.查询图片列表
        List<GraphVo> graphVoList=graphInfoMapper.selectListByItemIdAndItemType(id, ItemType.APARTMENT);

        //3.查询标签列表
        List<LabelInfo> labelInfoList=labelInfoMapper.selectListByApartmentId(id);
        //4.查询配套列表
        List<FacilityInfo> facilityInfoList=facilityInfoMapper.selectListByApartmentId(id);
        //5.查询杂费列表
        List<FeeValueVo> feeValueVoList=feeValueMapper.selectListByApartmentId(id);
        //6.组装结果
        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo,apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);

        return  apartmentDetailVo ;
    }

    @Override
    public void removeApartmentById(Long id) {
        LambdaQueryWrapper<RoomInfo> query = new LambdaQueryWrapper<>();
        query.eq(RoomInfo::getApartmentId,id);
        Long count = roomInfoMapper.selectCount(query);
        if(count>0){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_EXIST_ERROR.getCode(),"该公寓下有正在发布的房间，请先删除该公寓下的所有房间");
        }

        super.removeById(id);
        // 1.删除图片列表
        LambdaQueryWrapper<GraphInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        queryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoService.remove(queryWrapper);
        //2.删除配套列表
        LambdaQueryWrapper<ApartmentFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
        facilityQueryWrapper.eq(ApartmentFacility::getApartmentId,id);
        apartmentFacilityService.remove(facilityQueryWrapper);
        //3.删除标签列表
        LambdaQueryWrapper<ApartmentLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
        labelQueryWrapper.eq(ApartmentLabel::getApartmentId,id);
        apartmentLabelService.remove(labelQueryWrapper);

        //4.删除杂费列表
        LambdaQueryWrapper<ApartmentFeeValue> feeValueQueryWrapper = new LambdaQueryWrapper<>();
        feeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId,id);
        apartmentFeeValueService.remove(feeValueQueryWrapper);
    }
}




