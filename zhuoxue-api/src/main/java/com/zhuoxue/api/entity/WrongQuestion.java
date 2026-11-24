package com.zhuoxue.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wrong_question")
public class WrongQuestion {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long subjectId;

    private Long chapterId;

    private Long knowledgePointId;

    private String questionType;

    private Integer difficulty;

    private String content;

    private String myAnswer;

    private String correctAnswer;

    private String errorReason;

    private String aiAnalysis;

    private String aiSolution;

    private String masteryStatus;

    private Integer reviewStage;

    private Integer streak;

    private Integer reviewCount;

    private Integer correctCount;

    private Integer wrongCount;

    private LocalDateTime nextReviewTime;

    private LocalDateTime lastReviewTime;

    private String source;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
