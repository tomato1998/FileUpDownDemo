package com.boss.oss.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import com.boss.oss.service.FileService;
import com.boss.oss.utils.SnowFlake;
import com.boss.oss.utils.UploadThread;
import com.boss.oss.vo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author LiDaShan
 * @Version 1.0
 * @Date 2020/7/16
 * @Content:
 */
@RestController
@RequestMapping("/bigFile")
@Slf4j
public class MultiThreadFileUpDown {

    /**
     * 文件命名，访问文件名重复
     */
    private SnowFlake snowFlake = new SnowFlake(2,4);
    @Resource
    private FileService fileService;
    @Resource
    private OSS ossClient;
    private static String bucketName = "boss-demo";
    private static String firstKey = "big";



    @PostMapping("/{id}")
    public CommonResult Upload(@PathVariable("id") Integer id) throws IOException, InterruptedException, ExecutionException {
        long start = System.currentTimeMillis();
        String totalTime = "";
        long fileId = snowFlake.nextId();
        String objectName = firstKey+id+"-"+fileId+"png";
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
            创建容器存储线程返回的future
            开启5个线程，异步分片上传
            创建CountDownLatch计数器，监控各分片上传
         */
        final File sampleFile = new File("C:\\Users\\49072\\Desktop\\SpringCloud.mmap");
        List<Future<PartETag>> futures = new ArrayList();
        CountDownLatch latch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            futures.add(fileService.upload(latch,sampleFile, ossClient, i, bucketName, objectName, uploadId));
        }
        latch.await();
        for (Future<PartETag> future : futures) {
            partETags.add(future.get());
        }
        log.info("成功上传,partETags为:"+partETags);
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
         * 完成上传
         */
        CompleteMultipartUploadResult completeMultipartUploadResult =
                ossClient.completeMultipartUpload(completeMultipartUploadRequest);
        //ossClient.shutdown();
        return new CommonResult(666,"上传成功",totalTime+"      文件名:"+objectName);
    }


    /**
     * 文件下载
     * @param objectName
     * @return
     * @throws Throwable
     */
    @GetMapping("/{objectName}")
    public CommonResult download(@PathVariable("objectName") String objectName) throws Throwable {

        long start = System.currentTimeMillis();
        String totalTime = "";
        log.info("开始下载文件"+objectName);

        // 下载请求，10个任务并发下载，启动断点续传。
        DownloadFileRequest downloadFileRequest = new DownloadFileRequest(bucketName, objectName);
        downloadFileRequest.setDownloadFile("C:\\Users\\49072\\Desktop\\download\\"+objectName);
        downloadFileRequest.setPartSize(1 * 1024 * 1024);
        downloadFileRequest.setTaskNum(10);
        downloadFileRequest.setEnableCheckpoint(true);
        downloadFileRequest.setCheckpointFile("C:\\Users\\49072\\Desktop\\download\\"+objectName);

        // 下载文件。
        DownloadFileResult downloadRes = ossClient.downloadFile(downloadFileRequest);
        // 下载成功时，会返回文件元信息。
        ObjectMetadata objectMetadata = downloadRes.getObjectMetadata();

        long end = System.currentTimeMillis();
        log.info("文件下载结束时间：" + new Date());
        long useTime=(end-start)/1000;
        if(useTime<60){
            totalTime="耗时"+(useTime)+"s";
        }else{
            long MM=useTime/60;
            long ss=useTime%60;
            totalTime = "耗时"+(MM+"M:"+ss+"s");
        }
        // 关闭OSSClient。
        ossClient.shutdown();
        log.info("=================下载完成====================");
        return new CommonResult(666,"文件下载成功",objectName+"下载结束，文件元信息"+objectMetadata+"  "+totalTime);
    }

}
