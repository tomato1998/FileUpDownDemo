package com.boss.oss.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.concurrent.Callable;

/**
 * @author LiDaShan
 * @Version 1.0
 * @Date 2020/7/16
 * @Content:
 */
@Slf4j
public class UploadThread implements Callable<PartETag> {

    private File sampleFile;
    private OSS ossClient;
    private int tag;
    private String bucketName;
    private String objectName;
    private String uploadId;

    public UploadThread(File sampleFile,OSS ossClient,int tag,String bucketName,String objectName,String uploadId){
        this.sampleFile=sampleFile;
        this.ossClient=ossClient;
        this.tag=tag;
        this.bucketName=bucketName;
        this.objectName=objectName;
        this.uploadId=uploadId;
    }

    @Override
    public PartETag call() {

        log.info("====================================");
        log.info("=========线程"+tag+"已将开始上传==========");
        log.info("=========上传的分片id为"+tag+"==========");
        Long fileLength = sampleFile.length() / 5;
        Long partSize = fileLength / 5;
        long startPos = tag * partSize;
        long curPartSize = (tag + 1 == 5) ? (fileLength - startPos) : partSize;
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
        return uploadPartResult.getPartETag();
    }
}
