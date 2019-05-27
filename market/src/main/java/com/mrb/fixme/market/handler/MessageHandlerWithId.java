package com.mrb.fixme.market.handler;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.handler.BaseMessageHandler;

import java.nio.channels.AsynchronousSocketChannel;

public abstract class MessageHandlerWithId extends BaseMessageHandler {

    private final String id;
    private final String name;

    public MessageHandlerWithId(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected void rejectedMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        Utils.sendMessage(clientChannel, Core.rejectedMessage(fixMessage, message, id, name));
    }

    protected void executedMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        Utils.sendMessage(clientChannel, Core.executedMessage(fixMessage, message, id, name));
    }
}
