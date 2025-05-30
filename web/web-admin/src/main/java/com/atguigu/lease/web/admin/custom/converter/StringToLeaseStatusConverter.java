package com.atguigu.lease.web.admin.custom.converter;

import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.LeaseStatus;
import org.springframework.core.convert.converter.Converter;

public class StringToLeaseStatusConverter implements Converter<String, LeaseStatus> {
    @Override
    public LeaseStatus convert(String code) {

        LeaseStatus[] values = LeaseStatus.values();
        for(LeaseStatus  itemType : values){
            if(itemType.getCode().equals(Integer.valueOf(code))) return itemType;
        }

        throw new IllegalArgumentException("code:"+code+"非法");
    }
}
