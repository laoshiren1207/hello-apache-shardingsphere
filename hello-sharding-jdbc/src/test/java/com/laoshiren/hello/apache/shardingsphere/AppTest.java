package com.laoshiren.hello.apache.shardingsphere;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.laoshiren.hello.apache.shardingsphere.domain.TbOrder;
import com.laoshiren.hello.apache.shardingsphere.mapper.TbOrderMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * ProjectName:     hello-apache-shardingsphere
 * Package:         com.laoshiren.hello.apache.shardingsphere
 * ClassName:       AppTest
 * Author:          laoshiren
 * Git:             xiangdehua@pharmakeyring.com
 * Description:
 * Date:            2020/6/30 17:47
 * Version:         1.0.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class AppTest {



    @Test
    public void runEmpty(){

    }

    @Autowired
    private TbOrderMapper mapper;

    @Test
    public void selectAll (){
        QueryWrapper<TbOrder> queryWrapper = new QueryWrapper<>();
        List<TbOrder> tbOrders = mapper.selectList(queryWrapper);
        System.out.println(tbOrders);
    }

    @Test
    public void insertOrder(){
        for (int i = 0; i < 100 ; i++) {
            TbOrder tbOrder = new TbOrder();
            tbOrder.setUserId(Long.parseLong(""+i));
            tbOrder.setOrderId(new Double((Math.random()*2)).longValue());
            mapper.insert(tbOrder);
        }
    }

    @Test
    public void selectOne(){
        QueryWrapper<TbOrder> queryWrapper = new QueryWrapper<>();
        //queryWrapper.eq("id",220);
        queryWrapper.eq("user_id",32);
        List<TbOrder> tbOrders  = mapper.selectList(queryWrapper);
        System.out.println(tbOrders);
    }

}
