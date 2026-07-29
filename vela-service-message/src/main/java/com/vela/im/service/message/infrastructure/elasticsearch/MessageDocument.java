package com.vela.im.service.message.infrastructure.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "vela_message")
public class MessageDocument {

    @Id
    private Long messageKey;

    @Field(type = FieldType.Keyword)
    private String messageId;

    @Field(type = FieldType.Keyword)
    private String fromId;

    @Field(type = FieldType.Keyword)
    private String toId;

    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String messageBody;

    @Field(type = FieldType.Long)
    private Long messageTime;

    @Field(type = FieldType.Integer)
    private Integer appId;

    @Field(type = FieldType.Integer)
    private Integer conversationType;

    @Field(type = FieldType.Keyword)
    private String fromNickName;

    public Long getMessageKey() { return messageKey; }
    public void setMessageKey(Long messageKey) { this.messageKey = messageKey; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getFromId() { return fromId; }
    public void setFromId(String fromId) { this.fromId = fromId; }
    public String getToId() { return toId; }
    public void setToId(String toId) { this.toId = toId; }
    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }
    public Long getMessageTime() { return messageTime; }
    public void setMessageTime(Long messageTime) { this.messageTime = messageTime; }
    public Integer getAppId() { return appId; }
    public void setAppId(Integer appId) { this.appId = appId; }
    public Integer getConversationType() { return conversationType; }
    public void setConversationType(Integer conversationType) { this.conversationType = conversationType; }
    public String getFromNickName() { return fromNickName; }
    public void setFromNickName(String fromNickName) { this.fromNickName = fromNickName; }
}
