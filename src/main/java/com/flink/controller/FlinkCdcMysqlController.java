package com.flink.controller;

import com.flink.common.util.R;
import com.flink.domain.dto.TableMysqlToMysqlDTO;
import com.flink.domain.dto.ToManyTablesDTO;
import com.flink.service.TableMysqlToMysqlService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "flink cdc抽取mysql数据")
@RestController
@RequestMapping("flink/cdc/")
@CrossOrigin
public class FlinkCdcMysqlController {


    @Resource
    private TableMysqlToMysqlService toMysqlDsService;

    @ApiOperation("mysql同步单表结构和数据")
    @PostMapping("/toSingleTable")
    public R toSingleTable(@RequestBody TableMysqlToMysqlDTO dto){
        return toMysqlDsService.mySqlSync(dto);
    }

    @ApiOperation("mysql同步多表结构和数据")
    @PostMapping("/toManyTables")
    public R  toManyTables(@RequestBody ToManyTablesDTO dto){
         return  toMysqlDsService.toManyTables(dto);
    }

}
