package com.flink.controller;

import com.flink.common.util.R;
import com.flink.domain.dto.TableMysqlToEsDTO;
import com.flink.service.TableMysqlToEsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "flink cdc抽取mysql到es数据")
@RestController
@RequestMapping("flink/cdc/mysqlToEs")
@CrossOrigin
public class FlinkCdcMysqlToEsController {

    @Resource
    private TableMysqlToEsService tableMysqlToEsService;

    @ApiOperation("es同步单表结构和数据")
    @PostMapping("/singleTableToEs")
    public R singleTableToEs(@RequestBody TableMysqlToEsDTO dto){
        return tableMysqlToEsService.singleTableToEs(dto);
    }


}
