package com.flink.service;

import com.flink.domain.dto.SyncDTO;

public interface TablesMysqlToMysqlDsService {
    void mySqSync(SyncDTO dto);
}
