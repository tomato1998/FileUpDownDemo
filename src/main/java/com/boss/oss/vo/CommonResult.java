package com.boss.oss.vo;

/**
 * @author LiDaShan
 * @Version 1.0
 * @Date 2020/7/15
 * @Content:
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 页面返回结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult<T> {

    private Integer code;
    private String msg;
    private T data;

    public CommonResult(Integer code,String msg){
        this(code,msg,null);
    }
}
