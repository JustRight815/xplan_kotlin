package com.zh.xplan.ui.robot.model

import java.util.*

/**
 * 展示聊天消息的模型
 */
class ChatMessage {
    var msg: String? = null// 消息内容
    var type: Type? = null// 消息类型，SERVICE 客服消息，CUSTOMER 客户消息
    var date: Date? = null// 日期
    var state: Int = 0 //消息发送状态   0发送中  1发送成功   -1 发送失败

    enum class Type {
        SERVICE, CUSTOMER
    }

    constructor() {}

    constructor(msg: String, type: Type, date: Date) {
        this.msg = msg
        this.type = type
        this.date = date
    }

}
