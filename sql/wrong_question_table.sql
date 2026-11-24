-- ---------- 错题表：系统的核心数据 ----------
CREATE TABLE IF NOT EXISTS wrong_question (
  id                BIGINT       NOT NULL AUTO_INCREMENT,
  user_id           BIGINT       NOT NULL,                    -- 错题属于哪个学生
  subject_id        BIGINT       DEFAULT NULL,                -- 关联学科
  chapter_id        BIGINT       DEFAULT NULL,                -- 关联章节
  knowledge_point_id BIGINT      DEFAULT NULL,                -- 关联知识点
  question_type     VARCHAR(30)  NOT NULL DEFAULT '解答题' COMMENT '题型',
  difficulty        TINYINT      NOT NULL DEFAULT 3 COMMENT '难度 1-5',
  content           TEXT         NOT NULL COMMENT '题目内容',
  my_answer         TEXT         DEFAULT NULL COMMENT '我的答案',
  correct_answer    TEXT         DEFAULT NULL COMMENT '正确答案',
  error_reason      TEXT         DEFAULT NULL COMMENT '错因描述',
  ai_analysis       TEXT         DEFAULT NULL COMMENT 'AI 错因分析',     -- AI 服务写回
  ai_solution       TEXT         DEFAULT NULL COMMENT 'AI 解题思路',     -- AI 服务写回
  mastery_status    VARCHAR(20)  NOT NULL DEFAULT 'UNMASTERED' COMMENT 'UNMASTERED/VAGUE/MASTERED', -- 掌握状态
  review_stage      INT          NOT NULL DEFAULT 0 COMMENT '间隔重复阶段', -- 复习进行到第几档
  streak            INT          NOT NULL DEFAULT 0 COMMENT '连续记住次数',
  review_count      INT          NOT NULL DEFAULT 0,          -- 累计复习次数
  correct_count     INT          NOT NULL DEFAULT 0,          -- 累计答对次数
  wrong_count       INT          NOT NULL DEFAULT 0,          -- 累计答错次数
  next_review_time  DATETIME     DEFAULT NULL,                -- 下次该复习的时间
  last_review_time  DATETIME     DEFAULT NULL,                -- 上次复习时间
  source            VARCHAR(50)  DEFAULT '手动录入' COMMENT '来源',
  create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted           TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_q_user_time (user_id, create_time),        -- 加速「查某人的错题按时间排」
  KEY idx_q_user_review (user_id, next_review_time), -- 加速「查某人今天该复习什么」
  KEY idx_q_user_point (user_id, knowledge_point_id),
  KEY idx_q_user_subject (user_id, subject_id)
) ENGINE = InnoDB COMMENT ='错题本';