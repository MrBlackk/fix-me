package com.mrb.fixme.broker;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Broker {

    private final ByteBuffer readBuffer = ByteBuffer.allocate(4096);
    private AsynchronousSocketChannel socketChannel;
    private String brokerId = "0";

    private void start() {
        System.out.println("Broker turned ON");
        try {
            getSocketChannel().read(readBuffer, null, new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer result, Object attachment) {
                    final String m = Utils.read(result, readBuffer);
                    if (m.length() == 0) {
                        System.out.println("Message router died! Have to reconnect somehow");
                        socketChannel = null;
                    } else {
                        System.out.println("Server message: " + m);
                    }
                    getSocketChannel().read(readBuffer, null, this);
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    System.out.println("Reading failed");
                }
            });

            final Scanner scanner = new Scanner(System.in);
            while (true) {
                final String message = scanner.nextLine();
                if ("q".equals(message)) {
                    System.out.println("Quit");
                    break;
                }
                final Future<Integer> result = Utils.sendMessage(getSocketChannel(), message);
                System.out.println("Result: " + result.get());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("Broker turned OFF");
    }

    public AsynchronousSocketChannel getSocketChannel() {
        if (socketChannel == null) {
            try {
                socketChannel = AsynchronousSocketChannel.open();
                final Future future = socketChannel.connect(new InetSocketAddress(Core.HOST_NAME, Core.BROKER_PORT));
                future.get();
                brokerId = Utils.readMessage(getSocketChannel(), readBuffer);
                System.out.println("My current id: " + brokerId);
            } catch (IOException | InterruptedException | ExecutionException e) {
                System.out.println("Could not connect to Message Router, reconnecting...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                socketChannel = null;
                return getSocketChannel();
            }
        }
        return socketChannel;
    }

    public static void main(String[] args) {
        new Broker().start();
    }
}
