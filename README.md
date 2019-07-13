## fix-me
42 school project

## About
Simulation tools for the financial markets that exchange a simplified version of FIX messages.
The tools are able to communicate over a network using the TCP protocol.

3 independent components that comunicate over the network:
- A market component.
- A broker component.
- A message router.

## Build
Build the project running the command bellow in the root of your project folder.
This will generate runnable .jar files that can launch each component.
```
mvn clean package
```

## Router
The router is the central component of applications.
All other components connect to it in order to send messages to other components.
The router performs no buiness logic, it just dispatch messages to the destination component(s).
The router accepts incomming connections from multiple brokers and markets.

The router listen on 2 ports:
- Port 5000 for messages from Broker components.
- Port 5001 for messages from Market components.

When a Broker/Market establishes the connection the Router asigns it a unique 6 digit ID and communicates the ID to the Broker/Market.

Brokers and Markets include the assigned ID in all messages for identification and the Router uses the ID to create the routing table.
Once the Router receives a message it will perform 3 steps: 
- Validate the message based on the checkshum.
- Identify the destination in the routing table.
- Forward the message.

## Broker
The Broker sends two types of messages:
- Buy: An order where the broker wants to buy an instrument
- Sell: An order where the broker want to sell an instrument

and receives from the market messages of the following types:
- Exeuted: when the order was accepted by the market and the action succeeded
- Rejected: when the order could not be met

## Market
A market has a list of instruments that can be traded.
When orders are received from brokers the market tries to execute it.
If the execution is successfull, it updates the internal instrument list and sends the broker an Executed message.
If the order can’t be met, the market sends a Rejected message.

## FIX Messages
All messages respect the FIX notation.
All messages start with the ID asigned by the router and ended by the checksum.
Buy and Sell messages will have the following mandatory fields:
- Instrument
- Quantity
- Market
- Price
