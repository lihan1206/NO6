package com.cl.dao;

import com.cl.entity.TongzhisongjiliaoEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.TongzhisongjiliaoView;


/**
 * 通知发送记录
 * 
 * @author 
 * @email 
 * @date
 */
public interface TongzhisongjiliaoDao extends BaseMapper<TongzhisongjiliaoEntity> {
	
	List<TongzhisongjiliaoView> selectListView(@Param("ew") Wrapper<TongzhisongjiliaoEntity> wrapper);

	List<TongzhisongjiliaoView> selectListView(Pagination page,@Param("ew") Wrapper<TongzhisongjiliaoEntity> wrapper);
	
	TongzhisongjiliaoView selectView(@Param("ew") Wrapper<TongzhisongjiliaoEntity> wrapper);
	

}
