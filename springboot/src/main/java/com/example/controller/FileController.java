package com.example.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.Result;
import com.example.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * 文件接口
 * 安全说明：
 * 1) 读取（GET）由 JwtInterceptor 按请求方法放行——图片通过 img src 加载，无法携带 token；
 * 2) 上传（POST /upload、/editor/upload）与删除（DELETE /{flag}）均需登录态；
 * 3) 上传做后缀白名单校验，文件名由服务端重新生成，不保留客户端原始文件名。
 */
@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    // 文件上传存储路径
    private static final String filePath = System.getProperty("user.dir") + "/files/";

    // 允许上传的图片后缀白名单
    private static final List<String> ALLOWED_EXT = Arrays.asList("png", "jpg", "jpeg", "gif", "webp", "bmp");

    // 上传文件的访问地址前缀，由配置决定走后端直连还是 Nginx 同源代理
    @Value("${file.access-url:http://localhost:9090/files/}")
    private String fileAccessUrl;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        String fileName = this.save(file);
        return Result.success(fileAccessUrl + fileName);
    }

    /**
     * 富文本文件上传
     */
    @PostMapping("/editor/upload")
    public Dict editorUpload(MultipartFile file) {
        String fileName = this.save(file);
        return Dict.create().set("errno", 0)
                .set("data", CollUtil.newArrayList(Dict.create().set("url", fileAccessUrl + fileName)));
    }

    /**
     * 保存上传文件：后缀白名单校验 → 生成服务端文件名 → 落盘
     */
    private String save(MultipartFile file) {
        String fileName = this.buildFileName(file.getOriginalFilename());
        try {
            if (!FileUtil.isDirectory(filePath)) {
                FileUtil.mkdir(filePath);
            }
            FileUtil.writeBytes(file.getBytes(), filePath + fileName);
            log.info("文件上传成功: {}", fileName);
        } catch (Exception e) {
            log.error("文件上传失败: " + fileName, e);
            throw new CustomException("500", "文件上传失败");
        }
        return fileName;
    }

    /**
     * 生成服务端文件名：时间戳 + UUID + 后缀。
     * 不保留客户端原始文件名，避免路径穿越、特殊字符与同名覆盖。
     */
    private String buildFileName(String originalFilename) {
        String ext = FileUtil.extName(originalFilename);
        if (StrUtil.isBlank(ext) || !ALLOWED_EXT.contains(ext.toLowerCase())) {
            throw new CustomException("400", "不支持的文件类型，仅允许上传图片：" + ALLOWED_EXT);
        }
        return System.currentTimeMillis() + "-" + IdUtil.fastSimpleUUID() + "." + ext.toLowerCase();
    }

    /**
     * 校验路径参数，防止目录穿越（如 ../../application.yml）
     */
    private void checkFlag(String flag) {
        if (StrUtil.isBlank(flag) || flag.contains("..") || flag.contains("/") || flag.contains("\\")) {
            throw new CustomException("400", "非法的文件名");
        }
    }

    /**
     * 获取文件（由拦截器放行的只读接口）
     */
    @GetMapping("/{flag}")
    public void avatarPath(@PathVariable String flag, HttpServletResponse response) {
        OutputStream os;
        try {
            if (StrUtil.isNotEmpty(flag)) {
                this.checkFlag(flag);
                response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(flag, "UTF-8"));
                response.setContentType("application/octet-stream");
                byte[] bytes = FileUtil.readBytes(filePath + flag);
                os = response.getOutputStream();
                os.write(bytes);
                os.flush();
                os.close();
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.warn("文件读取失败: {}", flag);
        }
    }

    /**
     * 删除文件（需登录态）
     */
    @DeleteMapping("/{flag}")
    public void delFile(@PathVariable String flag) {
        this.checkFlag(flag);
        FileUtil.del(filePath + flag);
        log.info("文件删除成功: {}", flag);
    }

}
