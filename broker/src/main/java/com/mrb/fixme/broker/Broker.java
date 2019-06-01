package com.mrb.fixme.broker;

import com.mrb.fixme.broker.handler.ExecutionResult;
import com.mrb.fixme.broker.handler.ResultTagValidator;
import com.mrb.fixme.core.Client;
import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.exception.UserInputValidationException;
import com.mrb.fixme.core.handler.MessageHandler;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Broker extends Client {

    public static final String NAME_PREFIX = "B";

    private Broker(String name) {
        super(Core.BROKER_PORT, NAME_PREFIX + name);
    }

    private void start() {
        System.out.println("Broker turned ON");
        try {
            readFromSocket();

            final Scanner scanner = new Scanner(System.in);
            System.out.println("Message to send " + Core.USER_MESSAGE_FORMAT + ":");
            while (true) {
                try {
                    final String message = Core.userInputToFixMessage(scanner.nextLine(), getId(), getName());
                    final Future<Integer> result = Utils.sendMessage(getSocketChannel(), message);
                    result.get();
                } catch (UserInputValidationException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = super.getMessageHandler();
        final MessageHandler resultTag = new ResultTagValidator();
        final MessageHandler executionResult = new ExecutionResult();
        messageHandler.setNext(resultTag);
        resultTag.setNext(executionResult);
        return messageHandler;
    }

    public static void main(String[] args) {
        new Broker(Utils.getClientName(args)).start();
    }
}
