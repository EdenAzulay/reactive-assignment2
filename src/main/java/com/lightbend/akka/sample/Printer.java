package com.lightbend.akka.sample;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;


//#printer-messages
public class Printer extends AbstractActor {
//#printer-messages
  static public Props props() {
    return Props.create(Printer.class, () -> new Printer());
  }

  //#printer-messages
  static public class Greeting {
    public final String message;

    public Greeting(String message) {
      this.message = message;
    }
  }

  static public class Incoming_Msg {
    public final String msg;

    public Incoming_Msg(String msg) {
        this.msg = msg;
    }
  }
  //#printer-messages

  private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  public Printer() {
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Greeting.class, greeting -> {
            log.info(greeting.message);
        })
        .match(Incoming_Msg.class, m -> {
            log.info(m.msg);
            // System.out.println("text");
        })
        .build();
  }
//#printer-messages
}
//#printer-messages
