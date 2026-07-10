package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库与部门关联实体类。
 * 将知识库与部门进行多对多关联，替代原KnowledgeBase中的deptId字段。
 */
@Data
@TableName("knowledge_base_dept")
public class KnowledgeBaseDept implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;

    // 知识库ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long knowledgeBaseId;

    // 部门ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
