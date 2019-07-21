package net.ys.controller;

import com.luhuiguo.fastdfs.domain.StorePath;
import com.luhuiguo.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@RestController
public class UploadController {

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostMapping("upload")
    public String upload(MultipartFile myFile) throws IOException {
        //获取后缀名
        String extension = FilenameUtils.getExtension(myFile.getOriginalFilename());

        // group1:指storage服务器的组名
        // myFile.getInputStream():指这个文件中的输入流
        // myFile.getSize():文件的大小
        // 这一行是通过storageClient将文件传到storage容器
        StorePath uploadFile = storageClient.uploadFile("group1", myFile.getInputStream(), myFile.getSize(), extension);
        String sql = "INSERT INTO file ( file_name, group_name, file_path ) VALUES (?,?,?)";
        jdbcTemplate.update(sql, myFile.getOriginalFilename(), uploadFile.getGroup(), uploadFile.getPath());
        // 返回它在storage容器的的路径
        return uploadFile.getFullPath();
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<Map<String, Object>> query = jdbcTemplate.queryForList("SELECT * FROM file WHERE id = " + id);
        Map map = query.get(0);

        String groupName = map.get("group_name").toString();
        String filepath = map.get("file_path").toString();
        //从dfs中下载文件
        byte[] downloadFile = storageClient.downloadFile(groupName, filepath);

        //输出到浏览器
        String fileName = map.get("file_name").toString();
        String fileNameTemp;
        String agent = request.getHeader("USER-AGENT").toLowerCase();
        if (agent.indexOf("firefox") > -1) {
            fileNameTemp = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
        } else {
            fileNameTemp = URLEncoder.encode(fileName, "UTF-8");
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileNameTemp + "\"");//防止有空格
        response.getOutputStream().write(downloadFile);
    }
}
