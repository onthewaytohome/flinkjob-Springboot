package com.flink.service;

import com.flink.common.util.R;
import com.flink.domain.dto.TableMysqlToEsDTO;

public interface TableMysqlToEsService {

    R singleTableToEs(TableMysqlToEsDTO dto);

}
