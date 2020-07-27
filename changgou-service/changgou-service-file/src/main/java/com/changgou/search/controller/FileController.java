package com.changgou.search.controller;

import com.changgou.utils.FastDFSClient;
import entity.Result;
import entity.StatusCode;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 16:45 2019/8/12
 */
@RestController
public class FileController {

    /**
     * 文件上传
     * @param
     * @return
     * @throws Exception
     */
  /*  @PostMapping("/upload")
    public Result uploadFile(@RequestParam(value = "file") MultipartFile file) throws Exception{
        //封装文件对象属性
        String filename = file.getOriginalFilename();       //文件名字
        byte[] content = file.getBytes();                   //文件字节数组
        String ext = FilenameUtils.getExtension(filename);  //文件扩展名
        FastDFSFile fastDFSFile = new FastDFSFile(filename, content, ext);
        // 调用工具类：所在的组 以及 附件所在的目录
        String[] uploadResult = FastDFSClient.uploadFile(fastDFSFile);
        String url = FastDFSClient.getServerUrl()+"/"+uploadResult[0]+"/"+uploadResult[1];
        return new Result(true, StatusCode.OK,"文件上传成功",url);
    }*/

    /**
     * 文件上传
     * @param file
     * @return
     * @throws Exception
     */
   @PostMapping("/upload")
  public Result uploadFile(@RequestParam(value = "file")MultipartFile file) throws Exception{
       //文件字节数组
       byte[] file_buff = file.getBytes();
       //文件名字
       String filename = file.getOriginalFilename();
       //文件扩展名
       String file_etx_name = FilenameUtils.getExtension(filename);
       String[] uploadResult = FastDFSClient.uploadFile(file_buff, file_etx_name, "王五上传");
       String url = FastDFSClient.getServerUrl()+"/"+uploadResult[0]+"/"+uploadResult[1];
       return new Result(true,StatusCode.OK,"文件上传成功",url);
       //return url;
   }




}
