CREATE TABLE IF NOT EXISTS knowledge_point (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  subject_id  BIGINT       NOT NULL,
  chapter_id  BIGINT       NOT NULL,             -- 属于哪个章节
  name        VARCHAR(100) NOT NULL COMMENT '知识点名称',
  description VARCHAR(500) DEFAULT NULL COMMENT '知识点说明',
  sort        INT          NOT NULL DEFAULT 0,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_point (chapter_id, name),
  KEY idx_point_subject (subject_id),
  KEY idx_point_chapter (chapter_id)
) ENGINE = InnoDB COMMENT ='知识点';