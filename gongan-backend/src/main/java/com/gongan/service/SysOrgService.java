package com.gongan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongan.dto.PageResult;
import com.gongan.entity.SysOrg;

import java.util.List;
import java.util.Map;

/**
 * 机构服务接口
 */
public interface SysOrgService extends IService<SysOrg> {

    /**
     * 分页查询机构
     */
    PageResult<SysOrg> pageList(Map<String, Object> params);

    /**
     * 获取机构树
     */
    List<Map<String, Object>> getOrgTree();

    /**
     * 获取子机构列表
     */
    List<SysOrg> getChildren(Long parentId);
}
