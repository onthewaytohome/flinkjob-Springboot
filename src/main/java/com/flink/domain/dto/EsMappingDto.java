package com.flink.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "mapping入参")
public class EsMappingDto {
    @ApiModelProperty(value = "地址", required = true)
    private String address;

    @ApiModelProperty(value = "端口", required = true)
    private String port;

    @ApiModelProperty(value = "索引", required = true)
    private String indexName;

    @ApiModelProperty(value = "文本")
    private String q;

    private String loggedUser;
    private String ip;

    // 公共的静态工厂方法，用于生成EsToolDto实例并返回连接字符串
    public static String createEsUrl(String address, String port, String indexName) {
        return "http://" + address + ":" + port + "/" + indexName + "/_mapping";
    }

    // 公共的静态工厂方法，用于生成EsToolDto实例并返回连接字符串
    public static String detailEsUrl(EsMappingDto dto) {
        return "http://" + dto.getAddress() + ":" + dto.getPort() + "/" + dto.getIndexName() + "/_search";
    }

}