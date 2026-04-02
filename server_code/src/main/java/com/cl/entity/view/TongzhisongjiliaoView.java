package com.cl.entity.view;

import com.cl.entity.TongzhisongjiliaoEntity;

import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import java.io.Serializable;
import com.cl.utils.EncryptUtil;
 

/**
 * 通知发送记录
 * 后端返回视图实体辅助类   
 * （通常后端关联的表或者自定义的字段需要返回使用）
 * @author 
 * @email 
 * @date
 */
@TableName("tongzhisongjiliao")
public class TongzhisongjiliaoView  extends TongzhisongjiliaoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public TongzhisongjiliaoView(){
	}
 
 	public TongzhisongjiliaoView(TongzhisongjiliaoEntity tongzhisongjiliaoEntity){
 	try {
			BeanUtils.copyProperties(this, tongzhisongjiliaoEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
 		
	}



}
