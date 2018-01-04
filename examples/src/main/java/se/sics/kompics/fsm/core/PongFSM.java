/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics.fsm.core;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentProxy;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.Negative;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.fsm.BaseIdExtractor;
import se.sics.kompics.fsm.FSMBuilder;
import se.sics.kompics.fsm.FSMEvent;
import se.sics.kompics.fsm.FSMException;
import se.sics.kompics.fsm.FSMExternalState;
import se.sics.kompics.fsm.FSMInternalState;
import se.sics.kompics.fsm.FSMInternalStateBuilder;
import se.sics.kompics.fsm.FSMStateName;
import se.sics.kompics.fsm.MultiFSM;
import se.sics.kompics.fsm.OnFSMExceptionAction;
import se.sics.kompics.fsm.handler.FSMBasicEventHandler;
import se.sics.kompics.fsm.handler.FSMPatternEventHandler;
import se.sics.kompics.fsm.id.FSMIdentifier;
import se.sics.kompics.fsm.id.FSMIdentifierFactory;
import se.sics.kompics.util.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class PongFSM {

  private static final Logger LOG = LoggerFactory.getLogger(PongFSM.class);
  public static final String NAME = "pong-fsm";
  
  private static FSMBuilder.StructuralDefinition structuralDef() throws FSMException {
    return FSMBuilder.structuralDef()
      .onStart()
        .nextStates(States.WAIT_PATTERN_PING)
        .buildTransition()
      .onState(States.WAIT_BASIC_PING)
        .nextStates(States.WAIT_PATTERN_PING)
        .toFinal()
        .buildTransition()
      .onState(States.WAIT_PATTERN_PING)
        .nextStates(States.WAIT_BASIC_PING)
        .toFinal()
        .buildTransition();
  }

  private static FSMBuilder.SemanticDefinition semanticDef() throws FSMException {
    return FSMBuilder.semanticDef()
      .negativePort(PingPort.class)
        .basicEvent(BasicEvent.Ping.class)
          .subscribeOnStart(Handlers.handleBasicPing)
          .subscribe(Handlers.handleBasicPing, States.WAIT_BASIC_PING)
        .patternEvent(BasicEvent.Ping.class, PatternEvent.class)
          .subscribe(Handlers.handlePatternPing, States.WAIT_PATTERN_PING)
        .buildEvents();
  }
  
  private static final BaseIdExtractor bidx = new BaseIdExtractor() {
    @Override
    public Optional<Identifier> fromEvent(KompicsEvent event) throws FSMException {
      if (event instanceof PingFSM.Event) {
        return Optional.of(((PongFSM.Event)event).getPongBaseFSMId());
      }
      return Optional.empty();
    }
  };
  
  private static final OnFSMExceptionAction oexa = new OnFSMExceptionAction() {
    @Override
    public void handle(FSMException ex) {
      throw new RuntimeException(ex);
    }
  };
  
  public static MultiFSM multifsm(FSMIdentifierFactory fsmIdFactory, ExternalState es) throws FSMException {
    return FSMBuilder.multiFSM(fsmIdFactory, NAME, structuralDef(), semanticDef(), es, new InternalState.Builder(), 
      oexa, bidx);
  }
  
  private static enum States implements FSMStateName {
    WAIT_BASIC_PING, WAIT_PATTERN_PING
  }
  
  public static interface Event extends FSMEvent {
    public Identifier getPongBaseFSMId();
  }
  
  public static class ExternalState implements FSMExternalState {

    ComponentProxy proxy;
    Negative<PingPort> pingPort;

    @Override
    public void setProxy(ComponentProxy proxy) {
      this.proxy = proxy;
      pingPort = proxy.getPositive(PingPort.class).getPair();
    }

    @Override
    public ComponentProxy getProxy() {
      return proxy;
    }
  }

  private static class InternalState implements FSMInternalState {

    final FSMIdentifier fsmId;

    private InternalState(FSMIdentifier fsmId) {
      this.fsmId = fsmId;
    }

    @Override
    public FSMIdentifier getFSMId() {
      return fsmId;
    }

    public static class Builder implements FSMInternalStateBuilder {

      @Override
      public FSMInternalState newState(FSMIdentifier fsmId) {
        return new InternalState(fsmId);
      }
    }
  }

  private static class Handlers {

    static FSMBasicEventHandler handleBasicPing
      = new FSMBasicEventHandler<ExternalState, InternalState, BasicEvent.Ping>() {
        @Override
        public FSMStateName handle(FSMStateName state, ExternalState es, InternalState is,
          BasicEvent.Ping ping) throws FSMException {
          LOG.trace("basic {}", ping);
          es.getProxy().trigger(ping.pong(), es.pingPort);
          return States.WAIT_PATTERN_PING;
        }
      };
    
    static FSMPatternEventHandler handlePatternPing 
      = new FSMPatternEventHandler<ExternalState, InternalState, BasicEvent.Ping>() {
      @Override
      public FSMStateName handle(FSMStateName state, ExternalState es, InternalState is, BasicEvent.Ping pong,
        PatternExtractor<Class, BasicEvent.Ping> container) throws FSMException {
        LOG.trace("pattern {}", pong);
          es.getProxy().trigger(new PatternEvent(pong.pong()), es.pingPort);
          return States.WAIT_BASIC_PING;
      }
    };
  }
}
