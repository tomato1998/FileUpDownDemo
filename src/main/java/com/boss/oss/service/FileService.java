package com.boss.oss.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PartETag;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * @author LiDaShan
 * @Version 1.0
 * @Date 2020/7/16
 * @Content:
 */
public interface FileService {

    public Future<PartETag> upload(CountDownLatch latch, File sampleFile, OSS ossClient, int tag, String bucketName, String objectName, String uploadId);
}
