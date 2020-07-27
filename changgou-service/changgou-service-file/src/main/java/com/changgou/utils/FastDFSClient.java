package com.changgou.utils;

import com.changgou.file.FastDFSFile;
import org.apache.commons.lang.text.StrTokenizer;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 16:15 2019/8/12
 * 封装FastDFS的api工具类
 */
public class FastDFSClient {

    /**
     *  初始化FastDFS配置
     */
    static {
        String config_name = new ClassPathResource("fdfs_client.conf").getPath();
        try {
            // 初始化FastDFS的配置文件
            ClientGlobal.init(config_name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param
     * @param  fastDFSFile:附件信息
     * @return java.lang.String[]
     * @return
     */
   /* public static String[] uploadFile(FastDFSFile fastDFSFile){
        try {
            byte[] file_buff = fastDFSFile.getContent();
            String File_ext_name = fastDFSFile.getExt();
            NameValuePair[] meta_list = new NameValuePair[1];
            meta_list[0] = new NameValuePair(fastDFSFile.getAuthor());
            //创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            //根据句trackerClient获得跟踪服务器端
            TrackerServer trackerServer = trackerClient.getConnection();
            //创建文件上传储存服务器
            StorageClient storageClient = new StorageClient(trackerServer,null);
            // 4、文件上传
            String[] uploadResult = storageClient.upload_file(file_buff, File_ext_name, meta_list);
            return uploadResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/


    /**
     * 上传文件
     * @param
     * @param
     * @param
     * @return
     */
    public static String[] uploadFile(byte[] file_buff,String file_ext_name,String des){
        try {
            NameValuePair[] meta_list = new NameValuePair[1];
            meta_list[0] = new NameValuePair(des);
            //创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            //根据句trackerClient获得跟踪服务器端
            TrackerServer trackerServer = trackerClient.getConnection();
            //创建文件上传储存服务器
            StorageClient storageClient = new StorageClient(trackerServer,null);
            String[] uploadResult = storageClient.upload_file(file_buff, file_ext_name, meta_list);
            return uploadResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 下载文件
     * @param
     * @param
     * @return
     */
    public static byte[] dowloadFile(String group_name,String remote_filename){
        try {
            //创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            //根据句trackerClient获得跟踪服务器端
            TrackerServer trackerServer = trackerClient.getConnection();
            //创建文件上传储存服务器
            StorageClient storageClient = new StorageClient(trackerServer,null);
            byte[] downloadFile = storageClient.download_file(group_name, remote_filename);
            return downloadFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 删除文件
     * @param group_name :组名
     * @param remote_filename :文件名
     */
    public static void deleteFile(String group_name,String remote_filename){
        try {
            //创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            //根据句trackerClient获得跟踪服务器端
            TrackerServer trackerServer = trackerClient.getConnection();
            //创建文件上传储存服务器
            StorageClient storageClient = new StorageClient(trackerServer,null);
            storageClient.delete_file(group_name, remote_filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取附件信息
     * @param group_name
     * @param remote_filename
     * @return
     */
    public static FileInfo getFileInfo(String group_name,String remote_filename){
        try {
            // 1、创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            // 2、通过跟踪服务器client获取服务器端
            TrackerServer trackerServer = trackerClient.getConnection();
            // 3、创建存储服务器客户端
            StorageClient storageClient = new StorageClient(trackerServer, null);
            // 4、获得信息
            FileInfo fileInfo = storageClient.get_file_info(group_name, remote_filename);
            return fileInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**获取存储服务器信息
     *
     * @param group_name
     * @return
     */
    public static StorageServer getStorageServerInfo(String group_name){
        try {
            // 1、创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            // 2、通过跟踪服务器client获取服务器端
            TrackerServer trackerServer = trackerClient.getConnection();
            // 3、创建存储服务器客户端
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer, group_name);
            return storeStorage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获得多个储存对象服务器
     * @return
     */
    public static ServerInfo[] getServerInfo(String group_name,String file_name){
        try {
            // 1、创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            // 2、通过跟踪服务器client获取服务器端
            TrackerServer trackerServer = trackerClient.getConnection();
            // 3、获得多个存储服务器
            ServerInfo[] storages = trackerClient.getFetchStorages(trackerServer, group_name, file_name);
            return storages;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取拼接上传图片的地址url
     * @return
     */
    public static String getServerUrl(){
        try {
            // 1、创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            // 2、通过跟踪服务器client获取服务器端
            TrackerServer trackerServer = trackerClient.getConnection();
            // 3、拼接服务器地址
            String hostAddress = trackerServer.getInetSocketAddress().getAddress().getHostAddress();
            int port = ClientGlobal.getG_tracker_http_port();
            String url = "http://" + hostAddress + ":" + port;
            return url;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
