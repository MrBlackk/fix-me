package com.mrb.fixme.router;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.handler.InternalMessageHandler;
import com.mrb.fixme.core.handler.MandatoryTagsValidator;
import com.mrb.fixme.core.handler.MessageHandler;
import com.mrb.fixme.core.handler.ChecksumValidator;
import com.mrb.fixme.router.handler.MessageProcessor;

public class MessageRouter {

    private static final int FAILED_MESSAGES_RETRY_TIMEOUT = 10000;
    private final AtomicInteger id = new AtomicInteger(Core.INITIAL_ID);
    private final Map<String, AsynchronousSocketChannel> routingTable = new ConcurrentHashMap<>();
    private final Map<String, String> failedToSendMessages = new ConcurrentHashMap<>();

    private void start() {
        System.out.println("Message Router turned ON");
        try {
            final MessageHandler messageHandler = getMessageHandler();

            final AsynchronousServerSocketChannel brokersListener = AsynchronousServerSocketChannel
                    .open()
                    .bind(new InetSocketAddress(Core.HOST_NAME, Core.BROKER_PORT));
            brokersListener.accept(null,
                    new ClientCompletionHandler(brokersListener, routingTable, id, messageHandler));

            final AsynchronousServerSocketChannel marketsListener = AsynchronousServerSocketChannel
                    .open()
                    .bind(new InetSocketAddress(Core.HOST_NAME, Core.MARKET_PORT));
            marketsListener.accept(null,
                    new ClientCompletionHandler(marketsListener, routingTable, id, messageHandler));
        } catch (IOException e) {
            System.out.println("Couldn't open socket");
        }
        while (true) {
            try {
                Thread.sleep(FAILED_MESSAGES_RETRY_TIMEOUT);
            } catch (InterruptedException ignored) {
            }
            tryToSendFailedMessages();
        }
    }

    private void tryToSendFailedMessages() {
        if (!failedToSendMessages.isEmpty()) {
            System.out.println("Trying to send failed messages...");
            failedToSendMessages.keySet().removeIf(targetName -> {
                final AsynchronousSocketChannel targetChannel = routingTable.get(targetName);
                if (targetChannel != null) {
                    System.out.println("Found message to resend " + targetName + ", sending message");
                    Utils.sendMessage(targetChannel, failedToSendMessages.get(targetName));
                    return true;
                }
                return false;
            });
        }
    }

    private MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = new InternalMessageHandler();
        final MessageHandler mandatoryTagsValidator = new MandatoryTagsValidator();
        final MessageHandler checksumValidator = new ChecksumValidator();
        final MessageHandler messageParser = new MessageProcessor(routingTable, failedToSendMessages);
        messageHandler.setNext(mandatoryTagsValidator);
        mandatoryTagsValidator.setNext(checksumValidator);
        checksumValidator.setNext(messageParser);
        return messageHandler;
    }

    public static void main(String[] args) {
        new MessageRouter().start();
    }
}
