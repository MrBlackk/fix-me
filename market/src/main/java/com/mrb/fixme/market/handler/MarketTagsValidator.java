package com.mrb.fixme.market.handler;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.FixTag;
import com.mrb.fixme.core.MessageType;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.exception.WrongFixTagException;

import java.nio.channels.AsynchronousSocketChannel;

public class MarketTagsValidator extends MessageHandlerWithId {

    public MarketTagsValidator(String clientId) {
        super(clientId);
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            Core.getFixValueByTag(message, FixTag.INSTRUMENT);
            final String quantity = Core.getFixValueByTag(message, FixTag.QUANTITY);
            final String price = Core.getFixValueByTag(message, FixTag.PRICE);

            Integer.parseInt(quantity);
            Integer.parseInt(price);

            final String type = Core.getFixValueByTag(message, FixTag.TYPE);
            if (MessageType.is(type)) {
                super.handle(clientChannel, message);
            } else {
                Utils.sendMessage(clientChannel, Core.rejectedMessage(message, "Wrong message type", getClientId()));
            }
        } catch (WrongFixTagException ex) {
            Utils.sendMessage(clientChannel, Core.rejectedMessage(message, "Wrong fix tags", getClientId()));
        } catch (NumberFormatException ex) {
            Utils.sendMessage(clientChannel, Core.rejectedMessage(message, "Wrong tags type", getClientId()));
        }
    }
}
