package com.mrb.fixme.core.handler;

import com.mrb.fixme.core.Utils;

import java.nio.channels.AsynchronousSocketChannel;

public class InternalMessageHandler extends BaseMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        if (!message.startsWith(Utils.INTERNAL_MESSAGE)) {
            super.handle(clientChannel, message);
        }
    }
}
