package com.mrb.fixme.broker.handler;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.FixTag;
import com.mrb.fixme.core.Result;
import com.mrb.fixme.core.exception.WrongFixTagException;
import com.mrb.fixme.core.handler.BaseMessageHandler;

import java.nio.channels.AsynchronousSocketChannel;

public class ResultTagValidator extends BaseMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        final String result;
        try {
            result = Core.getFixValueByTag(message, FixTag.RESULT);
        } catch (WrongFixTagException ex) {
            System.out.println(ex.getMessage());
            return;
        }
        if (Result.is(result)) {
            super.handle(clientChannel, message);
        } else {
            System.out.println("Wrong result type in message: " + message);
        }
    }
}
