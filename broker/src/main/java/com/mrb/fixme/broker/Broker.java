package com.mrb.fixme.broker;

import com.mrb.fixme.core.Client;
import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.exception.UserInputValidationException;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Broker extends Client {

    private Broker() {
        super(Core.BROKER_PORT);
    }

    private void start() {
        System.out.println("Broker turned ON");
        try {
            readFromSocket();

            final Scanner scanner = new Scanner(System.in);
            System.out.println("Message to send 'MARKET_ID BUY_OR_SELL INSTRUMENT_NAME QUANTITY PRICE':");
            while (true) {
                try {
                    final String message = Core.userInputToFixMessage(scanner.nextLine(), getId());
                    final Future<Integer> result = Utils.sendMessage(getSocketChannel(), message);
                    System.out.println("Result: " + result.get());
                } catch (UserInputValidationException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Broker().start();
    }
}
