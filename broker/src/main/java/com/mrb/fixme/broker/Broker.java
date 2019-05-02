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

    private void start() {
        System.out.println("Broker turned ON");
        try {
            final AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
            final Future future = channel.connect(new InetSocketAddress(Core.HOST_NAME, Core.BROKER_PORT));
            future.get();

            final ByteBuffer readBuffer = ByteBuffer.allocate(4096);
            final String id = Utils.readMessage(channel, readBuffer);
            System.out.print("My id: " + id);

            channel.read(readBuffer, null, new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer result, Object attachment) {
                    final String m = Utils.read(result, readBuffer);
                    System.out.println("Server message: " + m);
                    channel.read(readBuffer, null, this);
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                }
            });

            final Scanner scanner = new Scanner(System.in);
            while (true) {
                final String message = scanner.nextLine();
                if ("q".equals(message)) {
                    System.out.println("Quit");
                    break;
                }
                final Future<Integer> result = Utils.sendMessage(channel, message);
                System.out.println("Result: " + result.get());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("Broker turned OFF");
    }

    public static void main(String[] args) {
        new Broker().start();
    }
}
