package com.mrb.fixme.broker.handler;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.FixTag;
import com.mrb.fixme.core.handler.BaseMessageHandler;

import java.nio.channels.AsynchronousSocketChannel;

public class ExecutionResult extends BaseMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        final String result = Core.getFixValueByTag(message, FixTag.RESULT);
        System.out.println("Operation result: " + result);
        super.handle(clientChannel, message);
    }
}
