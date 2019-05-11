package com.mrb.fixme.market.handler;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;

import java.nio.channels.AsynchronousSocketChannel;

public class MessageExecutor extends MessageHandlerWithId {

    public MessageExecutor(String clientId) {
        super(clientId);
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        Utils.sendMessage(clientChannel, Core.executedMessage(message, "OK", getClientId()));
        super.handle(clientChannel, message);
    }
}
