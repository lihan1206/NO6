package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.utils.PageUtils;
import com.cl.entity.TongzhisongjiliaoEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.TongzhisongjiliaoView;


/**
 * 通知发送记录
 *
 * @author 
 * @email 
 * @date
 */
public interface TongzhisongjiliaoService extends IService<TongzhisongjiliaoEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<TongzhisongjiliaoView> selectListView(Wrapper<TongzhisongjiliaoEntity> wrapper);
   	
   	TongzhisongjiliaoView selectView(@Param("ew") Wrapper<TongzhisongjiliaoEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<TongzhisongjiliaoEntity> wrapper);
   	
   
}
