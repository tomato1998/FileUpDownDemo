package com.boss.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;
import com.boss.oss.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * @author LiDaShan
 * @Version 1.0
 * @Date 2020/7/16
 * @Content:
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {




    @Override
    @Async("uploadExecutor")
    public Future<PartETag> upload(CountDownLatch latch, File sampleFile, OSS ossClient, int tag, String bucketName, String objectName, String uploadId) {

        /**
         * 1. 计算文件大小，分片大小与标识
         */
        log.info("=========线程"+tag+"已将开始上传==========");
        log.info("=========上传的分片id为"+tag+"==========");
        Long fileLength = sampleFile.length() ;
        log.info("=========上传总文件大小为"+fileLength+"==========");
        Long partSize = fileLength / 5;
        long startPos = tag * partSize;
        long curPartSize = (tag + 1 == 5) ? (fileLength - startPos) : partSize;
        log.info("===========线程"+tag+"上传的分片"+tag+"的大小为"+curPartSize+"=================");

        /**
         * 2. 阿里OSS的分片上传代码
         */
        InputStream instream = null;
        try {
            instream = new FileInputStream(sampleFile);
            // 跳过已经上传的分片。
            instream.skip(startPos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        UploadPartRequest uploadPartRequest = new UploadPartRequest();
        uploadPartRequest.setBucketName(bucketName);
        uploadPartRequest.setKey(objectName);
        uploadPartRequest.setUploadId(uploadId);
        uploadPartRequest.setInputStream(instream);
        // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
        uploadPartRequest.setPartSize(curPartSize);
        // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
        uploadPartRequest.setPartNumber(tag + 1);
        // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
        UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
        // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
        PartETag partETag = uploadPartResult.getPartETag();
        /**
         * 3.验证分片上传成功，获取服务器返回的partETag并返回
         */
        log.info("线程"+tag+"的partETag"+partETag);
        //计数器减一
        latch.countDown();
        return new AsyncResult<>(partETag);
    }

}
