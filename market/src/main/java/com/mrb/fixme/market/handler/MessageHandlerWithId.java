package com.mrb.fixme.market.handler;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.FixTag;
import com.mrb.fixme.core.Result;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.db.Database;
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
        sendMessage(clientChannel, fixMessage, message, Result.Rejected);
    }

    protected void executedMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        sendMessage(clientChannel, fixMessage, message, Result.Executed);
    }

    private void sendMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message, Result result) {
        final String targetName = Core.getFixValueByTag(fixMessage, FixTag.SOURCE_NAME);
        if (isInsertMessagesToDb()) {
            Database.insert(
                    name,
                    targetName,
                    Core.getFixValueByTag(fixMessage, FixTag.TYPE),
                    Core.getFixValueByTag(fixMessage, FixTag.INSTRUMENT),
                    Core.getFixValueByTag(fixMessage, FixTag.PRICE),
                    Core.getFixValueByTag(fixMessage, FixTag.QUANTITY),
                    result.toString(),
                    message);
            Database.selectAll();
        }
        Utils.sendMessage(clientChannel, Core.resultFixMessage(message, id, name, targetName, result));
    }

    protected boolean isInsertMessagesToDb() {
        return false;
    }
}
