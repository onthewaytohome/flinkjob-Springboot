package com.flink.common.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String input = "{\"zdError\":\"中度缺陷: （可能影响诊疗决策或病历完整性)  \\n1. 现病史不完整  \\n    未描述患者症状的加重/缓解因素（如体位变化、用药等），未提及是否伴随发热、性交后出血等关键症状。  \\n    未记录是否在院外接受过治疗（如抗生素使用），缺乏诊疗经过描述。\\n\\n2. 既往史遗漏关键信息  \\n    高血压病史未描述控制情况（用药、血压水平），可能影响围术期管理。  \\n    \\\"有无食物过敏史\\\"表述模糊，需明确\\\"有\\\"或\\\"无\\\"。\\n\\n3. 辅助检查未完善  \\n    白带常规结果未记录（如pH值、白细胞、线索细胞、真菌等），影响阴道炎类型判断。  \\n    未行肿瘤标志物（如CA125、HE4）检查，附件包块性质不明需进一步鉴别。\\n\\n4. 专科检查不规范  \\n    Bishop评分与胎儿骨盆评分对绝经后患者无临床意义，属于不合理项目。  \\n    未描述附件包块的超声特征（如血流信号、囊实性），需结合影像学进一步分析。\\n\\n\\n\\n \",\"highError\":\"高危机错误: （危及诊疗安全或导致误诊)  \\n1. 主诉与现病史矛盾  \\n    主诉描述\\\"阴道分泌物增多，恶臭\\\"，但现病史中明确记载\\\"无分泌物增多，无异味\\\"，前后矛盾可能导致误诊。\\n\\n2. 附件包块未明确性质  \\n    妇科检查发现右侧附件区5x4cm包块，但未描述包块性质（囊性/实性）、活动度、压痛等关键信息，存在恶性肿瘤风险未排除。\\n\\n\\n\\n \",\"basics\":\"基础性错误: （信息缺失或逻辑性不足)  \\n1. 主诉时间描述模糊  \\n    \\\"持续3天\\\"未明确是症状出现时间还是加重时间，需具体描述（如\\\"阴道分泌物增多伴恶臭3天\\\"）。\\n\\n2. 月经史描述混乱  \\n    \\\"月经1，经血量\\\"不符合规范，应表述为\\\"绝经年龄（如绝经5年）\\\"，并说明绝经后症状（如激素替代治疗）。\\n\\n3. 婚育史不完整  \\n    未记录具体孕产次（如G1P1），可能影响妇科疾病风险评估。\\n\\n4. 生命体征缺失  \\n    体温（T）、心率（P）、呼吸（R）、血压（BP）数值未填写，违反病历书写基本规范。\\n\\n\\n\\n \",\"norm\":\"规范性错误: （信息缺失或逻辑性不足)  \\n1. 主诉时间描述模糊  \\n    \\\"持续3天\\\"未明确是症状出现时间还是加重时间，需具体描述（如\\\"阴道分泌物增多伴恶臭3天\\\"）。\\n\\n2. 月经史描述混乱  \\n    \\\"月经1，经血量\\\"不符合规范，应表述为\\\"绝经年龄（如绝经5年）\\\"，并说明绝经后症状（如激素替代治疗）。\\n\\n3. 婚育史不完整  \\n    未记录具体孕产次（如G1P1），可能影响妇科疾病风险评估。\\n\\n4. 生命体征缺失  \\n    体温（T）、心率（P）、呼吸（R）、血压（BP）数值未填写，违反病历书写基本规范。\\n\\n\\n\\n \"}";
        input=input.replaceAll(" ","");
        System.out.println(input);  // 输出: 左肺尖，
    }

    public static List<ExPeisPacsModelVO> parseStringToList(String input) {
        List<ExPeisPacsModelVO> list = new ArrayList<>();
        Pattern pattern = Pattern.compile("ExPeisPacsModelVO\\(position=(.*?), exceptionDate=(.*?), yichang=(.*?), diagnosesName=(.*?), reference=(.*?), advise=(.*?)\\)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String position = matcher.group(1).trim();
            String exceptionDate = matcher.group(2).trim();
            String yichang = matcher.group(3).trim();
            String diagnosesName = matcher.group(4).trim();
            String reference = matcher.group(5).trim();
            String advise = matcher.group(6).trim();

            ExPeisPacsModelVO vo = new ExPeisPacsModelVO(position, exceptionDate, yichang, diagnosesName, reference, advise);
            list.add(vo);
        }

        return list;
    }
}

class ExPeisPacsModelVO {
    private String position;
    private String exceptionDate;
    private String yichang;
    private String diagnosesName;
    private String reference;
    private String advise;

    public ExPeisPacsModelVO(String position, String exceptionDate, String yichang, String diagnosesName, String reference, String advise) {
        this.position = position;
        this.exceptionDate = exceptionDate;
        this.yichang = yichang;
        this.diagnosesName = diagnosesName;
        this.reference = reference;
        this.advise = advise;
    }

    @Override
    public String toString() {
        return "ExPeisPacsModelVO{" +
                "position='" + position + '\'' +
                ", exceptionDate='" + exceptionDate + '\'' +
                ", yichang='" + yichang + '\'' +
                ", diagnosesName='" + diagnosesName + '\'' +
                ", reference='" + reference + '\'' +
                ", advise='" + advise + '\'' +
                '}';
    }
}