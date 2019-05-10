package com.mrb.fixme.core.handler;

import java.nio.channels.AsynchronousSocketChannel;

public abstract class BaseMessageHandler implements MessageHandler {

    private MessageHandler next;

    @Override
    public final void setNext(MessageHandler next) {
        this.next = next;
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        if (next != null) {
            next.handle(clientChannel, message);
        }
    }
}
