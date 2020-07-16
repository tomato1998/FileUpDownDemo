package com.boss.oss.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import com.boss.oss.utils.SnowFlake;
import com.boss.oss.utils.UploadThread;
import com.boss.oss.vo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author LiDaShan
 * @Version 1.0
 * @Date 2020/7/15
 * @Content: 大文件的多线程分片下载
 */

@RestController
@Slf4j
@RequestMapping("/largeFile")
public class LargeFileUpDownController {

    @Resource
    private OSS ossClient;

    /**
     * 存储空间（Bucket）名
     */
    private static String bucketName = "boss-demo";
    /**
     * 保存文件名前缀
     */
    private static String firstKey = "large";
    @Resource
    private ThreadPoolExecutor threadPool;
    private SnowFlake snowFlake = new SnowFlake(2,4);
    // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
    private volatile List<PartETag> partETags =  new ArrayList<PartETag>();

    @PostMapping("/{id}")
    public CommonResult LargeFileUpload(@PathVariable("id") Integer id) throws IOException, InterruptedException, ExecutionException {
        //记录开始上传时间
        long start = System.currentTimeMillis();
        //记录总耗时
        String time = "";
        long fileId = snowFlake.nextId();
        String objectName = firstKey+id+"----"+fileId;
        // 创建InitiateMultipartUploadRequest对象。
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);

        // 初始化分片。
        InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
        // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个uploadId发起相关的操作，如取消分片上传、查询分片上传等。
        String uploadId = upresult.getUploadId();

        //线程池共5个线程，分成5片
        final File sampleFile = new File("C:\\Users\\49072\\Desktop\\SpringCloud.mmap");
        List<Callable<PartETag>> tasks = new ArrayList();
        // 遍历分片上传。
        for (int i = 0; i < 5; i++) {
            Callable<PartETag> task = new UploadThread(sampleFile, ossClient, i, bucketName, objectName, uploadId);
            //threadPool.submit(task);
            tasks.add(task);
        }


        List<Future<PartETag>> futures = threadPool.invokeAll(tasks);
//        List<Future<PartETag>> futures = threadPool.invokeAll(tasks);
        for (Future<PartETag> future : futures) {
            partETags.add(future.get());
        }
        log.info("成功上传,partETags为:");
        threadPool.shutdown();
        //判断是否所有线程已经执行完毕
        while (true){
            if (threadPool.isTerminated()) {
                //记录结束时间
                long end = System.currentTimeMillis();
                log.info("文件上传结束时间：" + new Date());
                long date=(end-start)/1000;
                if(date<60){
                    time="耗时"+((end-start)/1000)+"s";
                }else{
                    long MM=date/60;
                    long ss=date%60;
                    time = "耗时"+(MM+"M:"+ss+"s");
                }
                // 创建CompleteMultipartUploadRequest对象。
                // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。
                //当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
                CompleteMultipartUploadRequest completeMultipartUploadRequest =
                        new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);
                // 完成上传。
                CompleteMultipartUploadResult completeMultipartUploadResult =
                        ossClient.completeMultipartUpload(completeMultipartUploadRequest);
                // 关闭OSSClient。
                ossClient.shutdown();
                break;
            }

        }

        return new CommonResult(666,"上传成功",time);
    }


}
