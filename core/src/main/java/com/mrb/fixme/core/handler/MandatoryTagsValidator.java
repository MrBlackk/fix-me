package com.mrb.fixme.core.handler;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.FixTag;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.exception.WrongFixTagException;

import java.nio.channels.AsynchronousSocketChannel;

public class MandatoryTagsValidator extends BaseMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            final String sourceId = Core.getFixValueByTag(message, FixTag.SOURCE_ID);
            final String targetId = Core.getFixValueByTag(message, FixTag.TARGET_ID);
            final String checksum = Core.getFixValueByTag(message, FixTag.CHECKSUM);

            Integer.parseInt(sourceId);
            Integer.parseInt(targetId);
            Integer.parseInt(checksum);
            super.handle(clientChannel, message);
        } catch (WrongFixTagException ex) {
            Utils.sendInternalMessage(clientChannel, ex.getMessage());
        } catch (NumberFormatException ex) {
            Utils.sendInternalMessage(clientChannel, "SOURCE_ID, TARGET_ID, CHECKSUM Tags should be numbers: " + message);
        }
    }
}
