package com.flink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flink.domain.po.DbConnections;
import com.flink.service.DbConnectionsService;
import com.flink.mapper.DbConnectionsMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【db_connections】的数据库操作Service实现
* @createDate 2024-12-09 08:47:43
*/
@Service
public class DbConnectionsServiceImpl extends ServiceImpl<DbConnectionsMapper, DbConnections>
    implements DbConnectionsService{

}




