package com.cl.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.cl.utils.PageUtils;
import com.cl.utils.Query;


import com.cl.dao.TongzhisongjiliaoDao;
import com.cl.entity.TongzhisongjiliaoEntity;
import com.cl.service.TongzhisongjiliaoService;
import com.cl.entity.view.TongzhisongjiliaoView;

@Service("tongzhisongjiliaoService")
public class TongzhisongjiliaoServiceImpl extends ServiceImpl<TongzhisongjiliaoDao, TongzhisongjiliaoEntity> implements TongzhisongjiliaoService {

	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<TongzhisongjiliaoEntity> page = this.selectPage(
                new Query<TongzhisongjiliaoEntity>(params).getPage(),
                new EntityWrapper<TongzhisongjiliaoEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<TongzhisongjiliaoEntity> wrapper) {
		  Page<TongzhisongjiliaoView> page =new Query<TongzhisongjiliaoView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
	@Override
	public List<TongzhisongjiliaoView> selectListView(Wrapper<TongzhisongjiliaoEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public TongzhisongjiliaoView selectView(Wrapper<TongzhisongjiliaoEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
