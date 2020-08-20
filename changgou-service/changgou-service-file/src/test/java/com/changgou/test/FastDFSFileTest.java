package com.changgou.test;

import com.changgou.utils.FastDFSClient;
import org.apache.commons.io.IOUtils;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.ServerInfo;
import org.csource.fastdfs.StorageServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileOutputStream;
import java.util.Date;


@SpringBootTest()
@RunWith(SpringRunner.class)
public class FastDFSFileTest {




    /**
     * 测试下载文件
     *
     * @throws Exception
     */
    @Test
    public void testDowloadFile() throws Exception {
        String group_name = "group1";
        String remote_filename = "M00/00/00/wKjThF1WdAeARS2xAA3VG0zdyVY121.jpg";
        byte[] dowloadFile = FastDFSClient.dowloadFile(group_name, remote_filename);
        IOUtils.write(dowloadFile, new FileOutputStream("F:/ee/1.jpg"));
    }

    /**
     * 删除文件
     *
     * @throws Exception
     */
    @Test
    public void deleteFile() {
        String group_name = "group1";
        String remote_filename = "M00/00/00/wKjThF1WdAeARS2xAA3VG0zdyVY121.jpg";
        FastDFSClient.deleteFile(group_name, remote_filename);
    }


    /**
     * 测试获取附件信息
     */
    @Test
    public void getFileInfo() {
        String group_name = "group1";
        String remote_filename = "M00/00/00/wKjThF1WdAeARS2xAA3VG0zdyVY121.jpg";
        FileInfo fileInfo = FastDFSClient.getFileInfo(group_name, remote_filename);
        String ipAddr = fileInfo.getSourceIpAddr();
        long fileSize = fileInfo.getFileSize();
        Date date = fileInfo.getCreateTimestamp();
        long crc32 = fileInfo.getCrc32();
        System.out.println("附件签名：" + crc32);
        System.out.println("附件所在的服务器地址：" + ipAddr);
        System.out.println("附件大小：" + fileSize);
        System.out.println("附件创建日期：" + date);
    }


    /**
     * 获取储存服务器信息
     */
    @Test
    public void getStorageServerInfo() {
        String group_name = "group1";
        StorageServer storageServer = FastDFSClient.getStorageServerInfo(group_name);
        String hostAddress = storageServer.getInetSocketAddress().getAddress().getHostAddress();
        int port = storageServer.getInetSocketAddress().getPort();
        String hostName = storageServer.getInetSocketAddress().getHostName();
        int storePathIndex = storageServer.getStorePathIndex();
        System.out.println("存储服务器地址：" + hostAddress);
        System.out.println("存储服务器端口：" + port);
        System.out.println("存储服务器角标：" + storePathIndex);
        System.out.println("存储服务器地址：" + hostName);
    }

    /**
     * 获得多个储存对象
     */
    @Test
    public void getServerInfo() {
        String group_name = "group1";
        String remote_filename = "M00/00/00/wKjThF1WdAeARS2xAA3VG0zdyVY121.jpg";
        ServerInfo[] serverInfos = FastDFSClient.getServerInfo(group_name, remote_filename);
        ServerInfo group = serverInfos[0];
        String ipAddr = group.getIpAddr();
        int port = group.getPort();
        System.out.println("服务器IP：" + ipAddr);
        System.out.println("服务器端口：" + port);
    }

}
