package com.gongan.service.impl;

import com.gongan.exception.BusinessException;
import com.gongan.service.DocumentParserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文档解析服务实现
 */
@Slf4j
@Service
public class DocumentParserServiceImpl implements DocumentParserService {

    @Override
    public String parseDocument(Path filePath, String fileType) {
        return switch (fileType.toLowerCase()) {
            case "pdf" -> parsePdf(filePath);
            case "word" -> parseWord(filePath);
            case "txt" -> parseTxt(filePath);
            default -> throw BusinessException.of("不支持的文件类型: " + fileType);
        };
    }

    /**
     * 解析PDF文档
     */
    private String parsePdf(Path filePath) {
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("PDF解析失败: {}", filePath, e);
            throw BusinessException.of("PDF解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析Word文档
     */
    private String parseWord(Path filePath) {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XWPFDocument document = new XWPFDocument(fis)) {
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    content.append(text).append("\n");
                }
            }
            return content.toString();
        } catch (IOException e) {
            log.error("Word解析失败: {}", filePath, e);
            throw BusinessException.of("Word解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析TXT文档
     */
    private String parseTxt(Path filePath) {
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            log.error("TXT解析失败: {}", filePath, e);
            throw BusinessException.of("TXT解析失败: " + e.getMessage());
        }
    }
}
