package com.mrb.fixme.router.handler;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.FixTag;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.handler.BaseMessageHandler;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class MessageProcessor extends BaseMessageHandler {

    private final Map<String, AsynchronousSocketChannel> routingTable;

    public MessageProcessor(Map<String, AsynchronousSocketChannel> routingTable) {
        this.routingTable = routingTable;
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        System.out.println();
        System.out.println("Processing message: " + message);
        final String targetId = Core.getFixValueByTag(message, FixTag.TARGET_ID);
        final AsynchronousSocketChannel targetChannel = routingTable.get(targetId);
        if (targetChannel != null) {
            Utils.sendMessage(targetChannel, message);
            super.handle(clientChannel, message);
        } else {
            Utils.sendMessage(clientChannel, "No client with such id: " + targetId);
        }
    }
}
