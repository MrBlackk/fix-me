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
        final String targetName = Core.getFixValueByTag(message, FixTag.TARGET_NAME);
        final AsynchronousSocketChannel targetChannel = routingTable.get(targetName);
        if (targetChannel != null) {
            Utils.sendMessage(targetChannel, message);
            super.handle(clientChannel, message);
        } else {
            Utils.sendInternalMessage(clientChannel, "No connected client with such name: " + targetName);
        }
    }
}
