package com.mrb.fixme.market.handler;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.FixTag;
import com.mrb.fixme.core.MessageType;
import com.mrb.fixme.core.exception.WrongFixTagException;

import java.nio.channels.AsynchronousSocketChannel;

public class MarketTagsValidator extends MessageHandlerWithId {

    public MarketTagsValidator(String id, String name) {
        super(id, name);
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            Core.getFixValueByTag(message, FixTag.INSTRUMENT);
            final int price = Integer.parseInt(Core.getFixValueByTag(message, FixTag.PRICE));
            final int quantity = Integer.parseInt(Core.getFixValueByTag(message, FixTag.QUANTITY));
            if (quantity <= 0) {
                rejectedMessage(clientChannel, message, "Negative quantity");
                return;
            } else if (price <= 0) {
                rejectedMessage(clientChannel, message, "Negative price");
                return;
            }

            final String type = Core.getFixValueByTag(message, FixTag.TYPE);
            if (MessageType.is(type)) {
                super.handle(clientChannel, message);
            } else {
                rejectedMessage(clientChannel, message, "Wrong operation type");
            }
        } catch (WrongFixTagException ex) {
            rejectedMessage(clientChannel, message, "Wrong fix tags");
        } catch (NumberFormatException ex) {
            rejectedMessage(clientChannel, message, "Wrong value type");
        }
    }
}
