package com.flink.service;


import com.flink.common.util.R;
import com.flink.domain.dto.TableMysqlToMysqlDTO;
import com.flink.domain.dto.ToManyTablesDTO;


public interface TableMysqlToMysqlService {
    R mySqlSync(TableMysqlToMysqlDTO dto);

    R toManyTables(ToManyTablesDTO dto);
}
