package com.mrb.fixme.core.handler;

import java.nio.channels.AsynchronousSocketChannel;

public interface MessageHandler {

    void setNext(MessageHandler handler);

    void handle(AsynchronousSocketChannel clientChannel, String message);
}
