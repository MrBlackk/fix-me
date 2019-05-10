package com.mrb.fixme.core.handler;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.FixTag;
import com.mrb.fixme.core.Utils;

import java.nio.channels.AsynchronousSocketChannel;

public class ChecksumValidator extends BaseMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        final String calculatedChecksum = Core.calculateChecksum(Core.getMessageWithoutChecksum(message));
        final String messageChecksum = Core.getFixValueByTag(message, FixTag.CHECKSUM);
        final boolean isValidChecksum = calculatedChecksum.equals(messageChecksum);
        if (isValidChecksum) {
            super.handle(clientChannel, message);
        } else {
            Utils.sendMessage(clientChannel, "Invalid checksum for message: " + message);
        }
    }
}
