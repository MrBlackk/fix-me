package com.mrb.fixme.market.handler;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.FixTag;
import com.mrb.fixme.core.Result;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.handler.BaseMessageHandler;

import java.nio.channels.AsynchronousSocketChannel;

public class MessageExecutor extends BaseMessageHandler {

    private final String clientId;

    public MessageExecutor(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        final String brokerId = Core.getFixValueByTag(message, FixTag.SOURCE_ID);
        final String resultMessage = Core.resultFixMessage(Result.Executed.toString(), clientId, brokerId);
        Utils.sendMessage(clientChannel, resultMessage);
        super.handle(clientChannel, message);
    }
}
