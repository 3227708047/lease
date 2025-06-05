package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.ApartmentInfoMapper;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
        if(!graphVoList.isEmpty()){
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for(GraphVo graphVo : graphVoList){
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
        for (Long facilityInfoId : facilityInfoIds) {
            ApartmentFacility apartmentFacility = new ApartmentFacility();
            apartmentFacility.setFacilityId(facilityInfoId);
            apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
            facilityList.add(apartmentFacility);
        }
        apartmentLabelService.saveBatch(facilityList);

        //3.插入标签列表

        List<Long> labelIdList = apartmentSubmitVo.getLabelIds();
        if(!labelIdList.isEmpty()){
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
        if (!feeValueIdList.isEmpty()){
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
        return null;
    }
}




