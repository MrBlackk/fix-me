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
            final int price = Integer.parseInt(Core.getFixValueByTag(message, FixTag.PRICE));
            final int quantity = Integer.parseInt(Core.getFixValueByTag(message, FixTag.QUANTITY));
            if (quantity <= 0) {
                Utils.sendMessage(clientChannel, Core.rejectedMessage(message, "Negative quantity", getClientId()));
                return;
            } else if (price <= 0) {
                Utils.sendMessage(clientChannel, Core.rejectedMessage(message, "Negative price", getClientId()));
                return;
            }

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
