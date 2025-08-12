package com.flink.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.flink.common.util.MysqlMetaData;
import com.flink.common.util.R;
import com.flink.domain.dto.LoggedUserDto;
import com.flink.domain.po.DbConnections;
import com.flink.service.DbConnectionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 *
 */
@Api(tags = "文本转sql测试")
@CrossOrigin
@RestController
@RequestMapping("/textToSql")
public class TextToSqlController {


    @Autowired
    private DbConnectionsService dbConnectionsService;


    @ApiOperation("用户绑定连接")
    @PostMapping(value = "dbConnections")
    public R dbConnections(@RequestBody LoggedUserDto loggedUserDto) {
        List<DbConnections> dbConnectionsList = dbConnectionsService.list(new QueryWrapper<DbConnections>().lambda().eq(DbConnections::getLoggedUser, loggedUserDto.getUser()));
        return R.success(dbConnectionsList);
    }

    @ApiOperation("用户添加连接")
    @PostMapping(value = "addConn")
    public R addConn(@RequestBody DbConnections dbConnections) {
        String linkName = dbConnections.getLinkName();

        List<DbConnections> dbConnectionsList = dbConnectionsService.list(new QueryWrapper<DbConnections>().lambda().eq(DbConnections::getLoggedUser, dbConnections.getLoggedUser()));

        for (DbConnections connections : dbConnectionsList) {
            String name = connections.getLinkName();
            if (name.equals(linkName)){
                return R.error(name+"连接名已存在");
            }
        }
        boolean save = dbConnectionsService.save(dbConnections);
        return R.success(save==true?"保存成功":"保存失败");
    }

    @ApiOperation("修改连接")
    @PostMapping(value = "updateConn")
    public R updateConn(@RequestBody DbConnections dbConnections) {

        String linkName = dbConnections.getLinkName();
        Integer id = dbConnections.getId();

        List<DbConnections> dbConnectionsList = dbConnectionsService.list(new QueryWrapper<DbConnections>().lambda().eq(DbConnections::getLoggedUser, dbConnections.getLoggedUser()));

        for (DbConnections connections : dbConnectionsList) {
            String name = connections.getLinkName();
            Integer connectionId = connections.getId();
            if (name.equals(linkName)&&id!=connectionId){
                return R.error(name+"连接名已存在");
            }
        }
        boolean save = dbConnectionsService.updateById(dbConnections);
        return R.success(save==true?"修改成功":"修改失败");
    }

    @ApiOperation("删除连接")
    @DeleteMapping(value = "delConn/{id}")
    public R delConn(@PathVariable @ApiParam("连接id") Integer id) {
        boolean save = dbConnectionsService.removeById(id);
        return R.success(save==true?"删除成功":"删除失败");
    }

    @ApiOperation("mysql DB列表")
    @PostMapping("/mysqlFetchAllDatabases")
    public R mysqlFetchAllDatabases(@ApiParam(value = "数据库地址") @RequestParam String address,
                                    @ApiParam(value = "端口号") @RequestParam Integer port,
                                    @ApiParam(value = "用户名") @RequestParam String user,
                                    @ApiParam(value = "密码") @RequestParam String password) {

        R r = MysqlMetaData.fetchAllDatabases(address, port, user, password);

        return r;
    }

    @ApiOperation("mysql Table列表")
    @PostMapping("/mysqlFetchAllTables")
    public R mysqlFetchAllTables(@ApiParam(value = "数据库地址") @RequestParam String address,
                                 @ApiParam(value = "端口号") @RequestParam Integer port,
                                 @ApiParam(value = "用户名") @RequestParam String user,
                                 @ApiParam(value = "密码") @RequestParam String password,
                                 @ApiParam(value = "库名") @RequestParam String dbName) {

        R r = MysqlMetaData.fetchAllTables(address, port, user, password,dbName);

        return r;
    }

    @ApiOperation("mysql 表数据展示")
    @PostMapping("/mysqlShowData")
    public R mysqlShowData(@ApiParam(value = "数据库地址") @RequestParam String address,
                           @ApiParam(value = "端口号") @RequestParam Integer port,
                           @ApiParam(value = "用户名") @RequestParam String user,
                           @ApiParam(value = "密码") @RequestParam String password,
                           @ApiParam(value = "库名") @RequestParam String dbName,
                           @ApiParam(value = "表名") @RequestParam String tableName,
                           @ApiParam(value = "每页数据量",defaultValue = "10") @RequestParam Integer count,
                           @ApiParam(value = "页码",defaultValue = "1") @RequestParam Integer page) {
        R r = MysqlMetaData.showData(address,port,user,password,dbName,
                tableName,count,page);
        return r;
    }



    @ApiOperation("数据源选择")
    @PostMapping("/dbSelect")
    public R textToSql() throws Exception {
        String dbType = "MySQL,elasticsearch";
        String[] split = dbType.split(",");
        return R.success(split);
    }


    @ApiOperation("mysql 字符集数组")
    @PostMapping("/lisCharacter")
    public R lisCharacter() {
        String characters = "utf8mb4, utf8, latin1, big5, ascii, gbk";

        String[] split = characters.split(",");
        return R.success(split);
    }

    @ApiOperation("mysql 排序规则数组")
    @PostMapping("/listCollate")
    public R listCollate(@ApiParam(value = "字符集",defaultValue = "utf8mb4") @RequestParam String character) {

        String[] collations;
        switch (character) {
            case "utf8mb4":
                collations = new String[] {"utf8mb4_0900_ai_ci", "utf8mb4_bin", "utf8mb4_general_ci", "utf8mb4_unicode_ci"};
                break;
            case "utf8":
                collations = new String[] {"utf8_bin", "utf8_general_ci", "utf8_unicode_ci"};
                break;
            case "latin1":
                collations = new String[] {"latin1_bin", "latin1_general_ci", "latin1_swedish_ci"};
                break;
            case "big5":
                collations = new String[] {"big5_bin", "big5_chinese_ci"};
                break;
            case "ascii":
                collations = new String[] {"ascii_bin"};
                break;
            case "gbk":
                collations = new String[] {"gbk_chinese_ci"};
                break;
            default:
                collations = new String[] {};
        }
        return R.success(collations);
    }

}

