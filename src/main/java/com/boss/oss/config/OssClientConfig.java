package com.boss.oss.config;


import com.aliyun.oss.OSS;

import com.aliyun.oss.OSSClientBuilder;
import com.boss.oss.vo.CommonResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author LiDaShan
 * @Version 1.0
 * @Date 2020/7/15
 * @Content:
 */
@Configuration
public class OssClientConfig {

    /**
     * OSS对外服务的访问域名
     */
    private static final String ENDPOINT="oss-cn-beijing.aliyuncs.com";
    /**
     * 访问密钥，标识用户
     */
    private static final String ACCESSKEY="LTAI4GJ5zTLJ6CPZxAwdyBzs";
    /**
     * 访问密钥，用于加密签名字符串和OSS用来验证签名字符串的密钥
     */
    private static final String ACCESSKEYSECRET="Rh0K8uEJaZS9gZoJiHsuDLyMPHSN2s";

    /**
     * 注入OSSClient实例
     * @return
     */
    @Bean
    public OSS createOSSClient(){
        return new OSSClientBuilder().build(ENDPOINT,ACCESSKEY,ACCESSKEYSECRET);
    }
}
