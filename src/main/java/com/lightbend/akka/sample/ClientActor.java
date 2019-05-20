package com.lightbend.akka.sample;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.net.InetSocketAddress;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connected;
import akka.io.Tcp.ConnectionClosed;
import akka.io.Tcp.Received;
import akka.io.TcpMessage;
import akka.japi.Procedure;
import akka.util.ByteString;
import akka.actor.AbstractActor;

public class ClientActor extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private ActorRef tcpActor;
    final InetSocketAddress remote;
    private String name;

    public static Props props(InetSocketAddress remote, ActorRef tcpActor, String name) {
        return Props.create(ClientActor.class, remote, tcpActor, name);
    }

    public ClientActor(InetSocketAddress remote, ActorRef tcpActor, String name) {
        this.remote = remote;
        this.tcpActor = tcpActor;
        this.name = name;

        if (tcpActor == null) {
            tcpActor = Tcp.get(getContext().system()).manager();
        }
        // log.info("In ClientActor - sending message: connected");
        tcpActor.tell(TcpMessage.connect(remote), getSelf());
        
        // log.info("In ClientActor - after received message: connected");
    }

    @Override
    public Receive createReceive() {
    return receiveBuilder()
        .match(CommandFailed.class, msg -> {
            log.info("In ClientActor - received message: failed");
            getContext().stop(getSelf());
        })
        .match(Connected.class, msg -> {
            log.info("In ClientActor - received message: connected");

            getSender().tell(TcpMessage.register(getSelf()), getSelf());
            getContext().become(connected(getSender()));

            getSender().tell(TcpMessage.write(ByteString.fromArray(this.name.getBytes())), getSelf());
        
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
                log.info("In ClientActor - Received message: " + ((Received) msg).data().utf8String());
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

    public static class Failed {
    }

}
/*
    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof CommandFailed) {
            log.info("In ClientActor - received message: failed");
            getContext().stop(getSelf());

        } else if (msg instanceof Connected) {
            log.info("In ClientActor - received message: connected");

            getSender().tell(TcpMessage.register(getSelf()), getSelf());
            getContext().become(connected(getSender()));

            getSender().tell(TcpMessage.write(ByteString.fromArray("hello".getBytes())), getSelf());
        }
    }

    private Procedure<Object> connected(final ActorRef connection) {
        return new Procedure<Object>() {
            @Override
            public void apply(Object msg) throws Exception {

                if (msg instanceof ByteString) {
                    connection.tell(TcpMessage.write((ByteString) msg), getSelf());

                } else if (msg instanceof CommandFailed) {
                    // OS kernel socket buffer was full

                } else if (msg instanceof Received) {
                    log.info("In ClientActor - Received message: " + ((Received) msg).data().utf8String());

                } else if (msg.equals("close")) {
                    connection.tell(TcpMessage.close(), getSelf());

                } else if (msg instanceof ConnectionClosed) {
                    getContext().stop(getSelf());
                }
            }
        };
    }
    private Procedure<Object> connected(final ActorRef connection) {
        return new Procedure<Object>() {
            @Override
            public void apply(Object msg) throws Exception {

                if (msg instanceof ByteString) {
                    connection.tell(TcpMessage.write((ByteString) msg), getSelf());

                } else if (msg instanceof CommandFailed) {
                    // OS kernel socket buffer was full

                } else if (msg instanceof Received) {
                    log.info("In ClientActor - Received message: " + ((Received) msg).data().utf8String());

                } else if (msg.equals("close")) {
                    connection.tell(TcpMessage.close(), getSelf());

                } else if (msg instanceof ConnectionClosed) {
                    getContext().stop(getSelf());
                }
            }
        };
    }*/
