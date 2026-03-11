package com.gongan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gongan.entity.AiDocumentChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文档分块Mapper
 */
@Mapper
public interface AiDocumentChunkMapper extends BaseMapper<AiDocumentChunk> {

    /**
     * 根据知识库ID查询所有分块
     */
    @Select("SELECT * FROM ai_document_chunk WHERE kb_id = #{kbId} AND deleted = 0 ORDER BY doc_id, chunk_index")
    List<AiDocumentChunk> selectByKbId(@Param("kbId") Long kbId);

    /**
     * 根据文档ID删除分块
     */
    @Select("DELETE FROM ai_document_chunk WHERE doc_id = #{docId}")
    void deleteByDocId(@Param("docId") Long docId);
}
