package com.flink.controller;

import com.flink.common.util.R;
import com.flink.domain.dto.EsMappingDto;
import com.flink.domain.dto.EsToolDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "ES连接工具")
@RestController
@RequestMapping("mt/estool")
@CrossOrigin
public class EsConnectionToolController {

    /**
     * ES连接测试
     *
     * @param dto 地址 port端口
     * @return msg
     */
    @ApiOperation("ES连接测试")
    @PostMapping(value = "connectTestModern")
    public R<String> connectTestModern(@RequestBody EsToolDto dto) {
        try {
            URL url = new URL(EsToolDto.createEsUrl(dto.getAddress(), dto.getPort()));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为GET
            connection.setRequestMethod("GET");
            // 设置连接超时时间
            connection.setConnectTimeout(5000);
            // 设置读取超时时间
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
            } else {
                System.out.println("Failed to connect to Elasticsearch. Response Code: " + responseCode);
            }
            connection.disconnect();
            return R.success("连接成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.success(e.getMessage());
        }
        //return R.error("连接失败");
    }


    /**
     * ES索引集合
     *
     * @param dto 地址 port端口
     * @return List
     */
    @ApiOperation("ES索引集合")
    @PostMapping(value = "getEsIndexList")
    public R<List<String>> getEsIndexList(@RequestBody EsToolDto dto) {
        List<String> indicesList = new ArrayList<>();
        try {
            URL url = new URL(EsToolDto.createEsUrl(dto.getAddress(), dto.getPort()) + "/_cat/indices?v");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/plain");

            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            String output;
            while ((output = br.readLine()) != null) {//&& indicesList.size() < 50
                String[] parts = output.split("\\s+");
                if (parts.length >= 3) {
                    if (!"index".equals(parts[2])) {
                        indicesList.add(parts[2]);
                    }
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.success(indicesList);
    }

    /**
     * ES索引详情
     *
     * @param dto 索引名
     * @return String
     */
    @ApiOperation("ES索引详情")
    @PostMapping(value = "getIndexDetail")
    public R<List<String>> getIndexDetail(@RequestBody EsMappingDto dto) {
        try {
            String query = "{\"query\": {\"match_all\": {}}}";
            URL url = new URL(EsMappingDto.detailEsUrl(dto));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = query.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = connection.getResponseCode();
            if (code == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return R.success(response.toString(), "查询成功");
            } else {
                System.err.println("Error: " + code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.error("查询失败");
    }


    /**
     * 查询索引结构mapping
     *
     * @param dto 地址 port端口
     * @return String
     */
    @ApiOperation("查询索引结构_mapping")
    @PostMapping(value = "getEsMapping")
    public R<String> getEsMapping(@RequestBody EsMappingDto dto) {
        try {
            // 构建请求URL
            URL url = new URL(EsMappingDto.createEsUrl(dto.getAddress(), dto.getPort(), dto.getIndexName()));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream()), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            connection.disconnect();
            return R.success(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error querying Elasticsearch mapping: " + e.getMessage());
        }
        return R.error();
    }




    /**
     * ES脚本执行
     *
     * @param dto 地址 port端口
     * @return String
     */
    @ApiOperation("AI辅助ES脚本执行")
    @PostMapping(value = "scriptExecution")
    public R<String> scriptExecution(@RequestBody EsMappingDto dto) {
        //没有主体请求
        if (!dto.getQ().contains("{") || dto.getQ().contains("这里添加具体的查询条件")) {
            return R.success(noSubjectExecution(dto));
        }
        // 创建一个HTTP客户端
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 构建HTTP POST请求
        HttpPost httpPost = new HttpPost(EsMappingDto.detailEsUrl(dto));

        // 构造请求体
        String requestBody = extractQueryPart(dto.getQ());
      /*  if (requestBody.contains("source")) {
            requestBody = requestBody.replace("source", "_source");
        }*/
        // 将请求体设置到请求中
        httpPost.setEntity(new StringEntity(requestBody, "UTF-8"));
        httpPost.setHeader("Content-Type", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(new HttpHost(dto.getAddress(), Integer.parseInt(dto.getPort()), "http"), httpPost)) {
            // 获取响应实体
            String responseBody = EntityUtils.toString(response.getEntity());
            return R.success(responseBody);
           /* if (response.getStatusLine().getStatusCode() == 200) {
                return R.success(responseBody);
            }
            return R.error(response.getStatusLine().getStatusCode(),responseBody);*/
        } catch (IOException e) {
            // 异常处理
            e.printStackTrace();
            return R.error("Error occurred during the request: " + e.getMessage());
        }
    }

    /*
     * 截取请求主体
     */
    private static String extractQueryPart(String jsonString) {
        int startIndex = jsonString.indexOf("{");
        int endIndex = jsonString.lastIndexOf("}") + 1; // 包含最后一个大括号

        if (startIndex == -1) {
            return jsonString;
        }
        // 提取query部分
        String queryPart = jsonString.substring(startIndex, endIndex);

        // 添加外层的双引号
        return queryPart;
    }

    /*
     * 没主体请求
     */
    public static String noSubjectExecution(EsMappingDto dto) {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {

            String url;
            if (dto.getQ().contains("_mapping")) {//查看mapping
                url = "http://" + dto.getAddress() + ":" + dto.getPort() + "/" + dto.getIndexName() + "/_mapping";
            } else if (dto.getQ().contains("_search")) {//查看数据
                url = "http://" + dto.getAddress() + ":" + dto.getPort() + "/" + dto.getIndexName() + "/_search";
            } else if (dto.getQ().contains("_settings")) {//查看settings
                url = "http://" + dto.getAddress() + ":" + dto.getPort() + "/" + dto.getIndexName() + "/_settings";
            } else {//查看索引
                url = "http://" + dto.getAddress() + ":" + dto.getPort() + "/" + dto.getIndexName();
            }
            // 创建 GET 请求
            HttpGet httpGet = new HttpGet(url);

            // 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                // 获取响应实体
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    // 打印响应状态码
                    int statusCode = response.getStatusLine().getStatusCode();
                    System.out.println("Response Status: " + statusCode);

                    // 获取响应体
                    String responseBody = EntityUtils.toString(responseEntity);
                    System.out.println("Response Body: " + responseBody);
                    return responseBody;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
