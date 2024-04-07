package com.ffxiv.linerobot.service;

import com.ffxiv.linerobot.entity.LineUserProfile;

public interface ConversationService {
  String getReply(LineUserProfile lineUserProfile, String receiveText);

  //    確認當前是否有正在進行的會話
  boolean isConversationSessionExists(String userId);
}
