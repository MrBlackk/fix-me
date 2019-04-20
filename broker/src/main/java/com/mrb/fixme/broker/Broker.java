package com.mrb.fixme.broker;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class Broker {

    private void start() {
        System.out.println("Broker turned ON");
        try {
            final AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
            final Future future = channel.connect(new InetSocketAddress(Core.HOST_NAME, Core.BROKER_PORT));
            future.get();

            final ByteBuffer readBuffer = ByteBuffer.allocate(4096);
            final String id = Utils.readMessage(channel, readBuffer);
            System.out.println("My id: " + id);
            for (int i = 0; i < 50; i++) {
                final String message = id + "_BROKER_MESSAGE_#" + i;
                final byte[] byteMessage = message.getBytes();
                final ByteBuffer buffer = ByteBuffer.wrap(byteMessage);
                System.out.println("Sending: " + message);
                Future<Integer> result = channel.write(buffer);
                System.out.println("Result: " + result.get());
                Thread.sleep(2000);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Broker().start();
        while (true);
    }
}
