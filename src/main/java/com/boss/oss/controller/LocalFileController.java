package com.boss.oss.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.boss.oss.vo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.*;

/**
 * @author LiDaShan
 * @Version 1.0
 * @Date 2020/7/15
 * @Content:
 */
@Controller
@RequestMapping("/smallFile")
@Slf4j
public class LocalFileController {


    @Resource
    private OSS ossClient;

    /**
     * 存储空间（Bucket）名
     */
    private static String bucketName = "boss-demo";
    /**
     * 保存文件名前缀
     */
    private static String firstKey = "small";


    /**
     * 上传图片
     * @param id
     * @return
     */
    @PostMapping("/{id}")
    public CommonResult upload(@PathVariable("id") Integer id){

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                                                            firstKey+id,
                                                                 new File("C:\\Users\\49072\\Desktop\\test1.png"));
        log.info("putObjectRequest info:"+putObjectRequest);
        ossClient.putObject(putObjectRequest);
        return new CommonResult(666,"成功",null);
    }


    /**
     * 下载图片
     * @param id
     * @return
     * @throws IOException
     */
    @GetMapping("/{id}")
    public CommonResult download(@PathVariable("id") Integer id) throws IOException {
        OSSObject ossObject = ossClient.getObject(bucketName, firstKey+id);
        InputStream is = ossObject.getObjectContent();
        OutputStream os = new FileOutputStream("C:\\Users\\49072\\Desktop\\download\\"+firstKey+id);
        int len=0;
        byte[] b=new byte[10];
        while((len=is.read(b))!=-1){
            os.write(b,0,len);
        }
        os.flush();
        os.close();
        is.close();
        return new CommonResult(666,"成功",null);
    }


    /**
     * 删除图片
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public CommonResult delete(@PathVariable("id") Integer id){
        ossClient.deleteObject(bucketName, firstKey+id);
        System.out.println("删除Object：" + firstKey + id + "成功。");
        return new CommonResult(666,"成功",null);
    }



}
