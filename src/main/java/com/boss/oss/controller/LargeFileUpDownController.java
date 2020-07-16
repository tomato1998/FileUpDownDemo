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
    /**
     * 文件命名，访问文件名重复
     */
    private SnowFlake snowFlake = new SnowFlake(2,4);


    @PostMapping("/{id}")
    public CommonResult LargeFileUpload(@PathVariable("id") Integer id) throws IOException, InterruptedException, ExecutionException {

        /**
         * 记录开始时间与文件上传总耗时
         */
        long start = System.currentTimeMillis();
        String totalTime = "";
        /**
         * 生成文件名
         */
        long fileId = snowFlake.nextId();
        String objectName = firstKey+id+"-"+fileId+".mmap";
        /**
         * 阿里OSS的文件分片文件上传代码
         *          创建InitiateMultipartUploadRequest对象
         *          initiateMultipartUpload初始化分片。
         *          返回uploadId，它是分片上传事件的唯一标识，您可以根据这个uploadId发起相关的操作，如取消分片上传、查询分片上传等。
         *          partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
         */
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);
        InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
        String uploadId = upresult.getUploadId();
        List<PartETag> partETags =  new ArrayList<PartETag>();

        /**
         * 创建5个线程，添加进任务容器
         *      线程池maxPoolSize为5，所以创建5个
         *      tasks为任务容器
         */
        final File sampleFile = new File("C:\\Users\\49072\\Desktop\\SpringCloud.mmap");
        List<Callable<PartETag>> tasks = new ArrayList();
        for (int i = 0; i < 5; i++) {
            Callable<PartETag> task = new UploadThread(sampleFile, ossClient, i, bucketName, objectName, uploadId);
            tasks.add(task);
        }

        /**
         * 线程池开启5个线程，拥塞main线程，获取返回结果
         *      获取partETags，后续发送给OSS验证分片完整性
         */
        List<Future<PartETag>> futures = threadPool.invokeAll(tasks);
        for (Future<PartETag> future : futures) {
            partETags.add(future.get());
        }
        log.info("成功上传,partETags为:"+partETags);

        /**
         * 关闭线程池，记录上传结束时间，计算总耗时
         */
        //threadPool.shutdown();
        long end = System.currentTimeMillis();
        log.info("文件上传结束时间：" + new Date());
        long useTime=(end-start)/1000;
        if(useTime<60){
            totalTime="耗时"+(useTime)+"s";
        }else{
            long MM=useTime/60;
            long ss=useTime%60;
            totalTime = "耗时"+(MM+"M:"+ss+"s");
        }

        /**
         * 阿里OSS提交partETags进行分片验证代码
         *         创建CompleteMultipartUploadRequest对象。
         *         在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。
         *         当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
         */
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                        new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);

        /**
         * 完成上传，并关闭ossClient
         */
        CompleteMultipartUploadResult completeMultipartUploadResult =
                        ossClient.completeMultipartUpload(completeMultipartUploadRequest);
        //ossClient.shutdown();
        return new CommonResult(666,"上传成功",totalTime);
    }


}
