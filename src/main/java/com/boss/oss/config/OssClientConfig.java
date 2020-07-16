package com.boss.oss.config;


import com.aliyun.oss.OSS;

import com.aliyun.oss.OSSClientBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @Value("${oss.endpoint}")
    private String endpoint;
    /**
     * 访问密钥，标识用户
     */
    @Value("${oss.accesskey}")
    private String accesskey;
    /**
     * 访问密钥，用于加密签名字符串和OSS用来验证签名字符串的密钥
     */
    @Value("${oss.accesskeySecret}")
    private String accessKeySecret;

    /**
     * 注入OSSClient实例
     * @return
     */
    @Bean
    public OSS createOSSClient(){
        return new OSSClientBuilder().build(endpoint,accesskey,accessKeySecret);
    }
}
