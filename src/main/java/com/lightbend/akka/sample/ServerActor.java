package com.lightbend.akka.sample;

import java.net.InetSocketAddress;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.Tcp.Bound;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connected;
import akka.io.TcpMessage;
import akka.actor.AbstractActor;
import java.util.*;
import java.util.concurrent.TimeUnit;

import akka.io.Tcp.ConnectionClosed;
import akka.io.Tcp.Received;
import akka.japi.Procedure;
import akka.util.ByteString;

public class ServerActor extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private ActorRef tcpActor;
    private List<String> connections;

    public static Props props(ActorRef tcpActor) {
        return Props.create(ServerActor.class, tcpActor);
    }

    public ServerActor(ActorRef tcpActor) {
        this.tcpActor = tcpActor;
    }

    @Override
    public void preStart() throws Exception {
        if (tcpActor == null) {
            tcpActor = Tcp.get(getContext().system()).manager();
        }

        tcpActor.tell(TcpMessage.bind(getSelf(),
                new InetSocketAddress("localhost", 9090), 100), getSelf());
        this.connections = new ArrayList<>();
    }

    @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Bound.class, msg -> {
            log.info("In ServerActor - received message: bound");
        })
        .match(CommandFailed.class, msg -> {
            getContext().stop(getSelf());
        })
        .match(Login.class, msg -> {
            this.connections.add(msg.getName());
            log.info("In ServerActor - added connection: "+msg.getName());
        })
        .match(Received.class, msg -> {
            String message = ((Received) msg).data().utf8String();
            connections.add(message);
            log.info("In ServerActor - Received message: " + ((Received) msg).data().utf8String());
        })
        .match(Connected.class, msg -> {
            final Connected conn = (Connected) msg;
            log.info("In ServerActor - received message: connected");

            getSender().tell(TcpMessage.register(getSelf()), getSelf());
            // getContext().become(connected(getSender()));
            
            // getSender().tell(TcpMessage.write(ByteString.fromArray("hello".getBytes())), getSelf());
            
            // final ActorRef handler = getContext().actorOf(
            //         Props.create(SimplisticHandlerActor.class));

            // getSender().tell(TcpMessage.register(handler), getSelf());
        
        })
        .build();
    }
    public Receive connected(final ActorRef connection) {
        return receiveBuilder()
            .match(ByteString.class, msg -> {
                connection.tell(TcpMessage.write((ByteString) msg), getSelf());
            })
            .match(CommandFailed.class, msg -> {
            })
            .match(Received.class, msg -> {
                String message = ((Received) msg).data().utf8String();
                connections.add(message);
                log.info("In ServerActor - Received message: " + ((Received) msg).data().utf8String());
            })
            .match(String.class, msg -> {
                if (msg.equals("close")) {
                    connection.tell(TcpMessage.close(), getSelf());
                }
            })
            .match(ConnectionClosed.class, msg -> {
                getContext().stop(getSelf());
            })
            .build();
      }

}

    // @Override
    // public void onReceive(Object msg) throws Exception {
    //     if (msg instanceof Bound) {
    //         log.info("In ServerActor - received message: bound");

    //     } else if (msg instanceof CommandFailed) {
    //         getContext().stop(getSelf());

    //     } else if (msg instanceof Connected) {
    //         final Connected conn = (Connected) msg;
    //         log.info("In ServerActor - received message: connected");

    //         final ActorRef handler = getContext().actorOf(
    //                 Props.create(SimplisticHandlerActor.class));

    //         getSender().tell(TcpMessage.register(handler), getSelf());
    //     }
    // }