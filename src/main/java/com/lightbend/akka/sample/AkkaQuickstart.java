package com.lightbend.akka.sample;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import com.lightbend.akka.sample.ClientActor;
import com.lightbend.akka.sample.ServerActor;
import java.util.concurrent.TimeUnit;
import java.lang.String;
import java.net.InetSocketAddress;

public class AkkaQuickstart {
  public static void main(String[] args) {
    ActorSystem serverActorSystem = ActorSystem.create("ServerActorSystem");

        ActorRef serverActor = serverActorSystem.actorOf(ServerActor.props(null), "serverActor");

        ActorSystem clientActorSystem = ActorSystem.create("ClientActorSystem");
        ActorRef clientActor = clientActorSystem.actorOf(ClientActor.props(
                new InetSocketAddress("localhost", 9090), null, "Ohad"), "clientActor");
        ActorRef clientActor2 = clientActorSystem.actorOf(ClientActor.props(
                new InetSocketAddress("localhost", 9090), null, "Eden"), "clientActor2");

        serverActorSystem.whenTerminated();
        clientActorSystem.whenTerminated();
  }
}
