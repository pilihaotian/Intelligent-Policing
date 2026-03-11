package com.gongan.service;

import java.nio.file.Path;

/**
 * 文档解析服务接口
 */
public interface DocumentParserService {

    /**
     * 解析文档内容
     * @param filePath 文件路径
     * @param fileType 文件类型（pdf/word/txt）
     * @return 文档文本内容
     */
    String parseDocument(Path filePath, String fileType);
}
