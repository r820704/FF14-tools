
DROP TABLE IF EXISTS `line_user_profile`;
CREATE TABLE line_user_profile(
      channel varchar(30), -- 例: Messaging API
      user_id varchar(99),
      display_name varchar(100),
      language varchar(10),
      picture_url varchar(300),
      status_message varchar(300),
      PRIMARY KEY (channel, user_id)
      -- 每個chaneel所發行的userid不相同，所以pk為channel, user_id
);

DROP TABLE IF EXISTS `bot_conversation_config`;
CREATE TABLE bot_conversation_config(
      topic varchar(20), -- 對話主題
      parent_id varchar(10), -- 上一層選項id，首層選項時則為空
      conversation_id varchar(10), -- 選項id, 如何0則為開頭說明而不是選項
      detail varchar(200), -- 選項內容
      PRIMARY KEY (topic, conversation_id)
);