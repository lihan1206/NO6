package com.cl.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import com.cl.utils.ValidatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.cl.annotation.IgnoreAuth;
import com.cl.annotation.SysLog;

import com.cl.entity.TongzhisongjiliaoEntity;
import com.cl.entity.view.TongzhisongjiliaoView;

import com.cl.service.TongzhisongjiliaoService;
import com.cl.service.TokenService;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.MPUtil;
import com.cl.utils.MapUtils;
import com.cl.utils.CommonUtil;

/**
 * 通知发送记录
 * 后端接口
 * @author 
 * @email 
 * @date
 */
@RestController
@RequestMapping("/tongzhisongjiliao")
public class TongzhisongjiliaoController {
    @Autowired
    private TongzhisongjiliaoService tongzhisongjiliaoService;

    /**
     * 后台列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,TongzhisongjiliaoEntity tongzhisongjiliao,
                    HttpServletRequest request){
        EntityWrapper<TongzhisongjiliaoEntity> ew = new EntityWrapper<TongzhisongjiliaoEntity>();
        
        PageUtils page = tongzhisongjiliaoService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, tongzhisongjiliao), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,TongzhisongjiliaoEntity tongzhisongjiliao,
		HttpServletRequest request){
        EntityWrapper<TongzhisongjiliaoEntity> ew = new EntityWrapper<TongzhisongjiliaoEntity>();

		PageUtils page = tongzhisongjiliaoService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, tongzhisongjiliao), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( TongzhisongjiliaoEntity tongzhisongjiliao){
       	EntityWrapper<TongzhisongjiliaoEntity> ew = new EntityWrapper<TongzhisongjiliaoEntity>();
      	ew.allEq(MPUtil.allEQMapPre( tongzhisongjiliao, "tongzhisongjiliao")); 
        return R.ok().put("data", tongzhisongjiliaoService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(TongzhisongjiliaoEntity tongzhisongjiliao){
        EntityWrapper< TongzhisongjiliaoEntity> ew = new EntityWrapper< TongzhisongjiliaoEntity>();
 		ew.allEq(MPUtil.allEQMapPre( tongzhisongjiliao, "tongzhisongjiliao")); 
		TongzhisongjiliaoView tongzhisongjiliaoView =  tongzhisongjiliaoService.selectView(ew);
		return R.ok("查询通知发送记录成功").put("data", tongzhisongjiliaoView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        TongzhisongjiliaoEntity tongzhisongjiliao = tongzhisongjiliaoService.selectById(id);
		tongzhisongjiliao = tongzhisongjiliaoService.selectView(new EntityWrapper<TongzhisongjiliaoEntity>().eq("id", id));
        return R.ok().put("data", tongzhisongjiliao);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        TongzhisongjiliaoEntity tongzhisongjiliao = tongzhisongjiliaoService.selectById(id);
		tongzhisongjiliao = tongzhisongjiliaoService.selectView(new EntityWrapper<TongzhisongjiliaoEntity>().eq("id", id));
        return R.ok().put("data", tongzhisongjiliao);
    }
    
    /**
     * 后端保存
     */
    @RequestMapping("/save")
    @SysLog("新增通知发送记录")
    public R save(@RequestBody TongzhisongjiliaoEntity tongzhisongjiliao, HttpServletRequest request){
    	tongzhisongjiliaoService.insert(tongzhisongjiliao);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @SysLog("新增通知发送记录")
    @RequestMapping("/add")
    public R add(@RequestBody TongzhisongjiliaoEntity tongzhisongjiliao, HttpServletRequest request){
    	tongzhisongjiliaoService.insert(tongzhisongjiliao);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    @SysLog("修改通知发送记录")
    public R update(@RequestBody TongzhisongjiliaoEntity tongzhisongjiliao, HttpServletRequest request){
        tongzhisongjiliaoService.updateById(tongzhisongjiliao);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @SysLog("删除通知发送记录")
    public R delete(@RequestBody Long[] ids){
        tongzhisongjiliaoService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    

}
