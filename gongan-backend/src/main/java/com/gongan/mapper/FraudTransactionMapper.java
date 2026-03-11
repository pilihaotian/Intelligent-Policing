package com.gongan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gongan.entity.FraudTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FraudTransactionMapper extends BaseMapper<FraudTransaction> {}
