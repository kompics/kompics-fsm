/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics.fsm.core;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentProxy;
import se.sics.kompics.Negative;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.Positive;
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
public class PingFSM {

  private static final Logger LOG = LoggerFactory.getLogger(PingFSM.class);
  public static final String NAME = "ping-fsm";

  private static FSMBuilder.StructuralDefinition structuralDef() throws FSMException {
    return FSMBuilder.structuralDef()
      .onStart()
        .nextStates(States.WAIT_BASIC_PONG)
        .buildTransition()
      .onState(States.WAIT_BASIC_PONG)
        .nextStates(States.WAIT_PATTERN_PONG)
        .toFinal()
        .buildTransition()
      .onState(States.WAIT_PATTERN_PONG)
         .nextStates(States.WAIT_BASIC_PONG)
        .toFinal()
        .buildTransition();
  }

  private static FSMBuilder.SemanticDefinition semanticDef() throws FSMException {
    return FSMBuilder.semanticDef()
      .negativePort(PingCtrlPort.class)
        .onBasicEvent(StartPingEvent.class)
          .subscribeOnStart(Handlers.handleStart)
        .buildEvents()
      .positivePort(PingPort.class)
        .onBasicEvent(BasicEvent.Pong.class)
          .subscribe(Handlers.handleBasicPong, States.WAIT_BASIC_PONG)
        .onPatternEvent(BasicEvent.Pong.class, PatternEvent.class)
          .subscribe(Handlers.handlePatternPong, States.WAIT_PATTERN_PONG)
        .buildEvents();
  }
  
  private static final BaseIdExtractor bidx = new BaseIdExtractor() {
    @Override
    public Optional<Identifier> fromEvent(FSMEvent event) throws FSMException {
      if (event instanceof Event) {
        return Optional.of(((Event)event).getPingBaseFSMId());
      }
      return Optional.absent();
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

  public static void start(ExternalState es, Identifier pingBaseFSMId, Identifier pongBaseFSMId) {
    es.proxy.trigger(new StartPingEvent(pingBaseFSMId, pongBaseFSMId), es.pingCtrlPort.getPair());
  }

  public static interface Event extends FSMEvent {

    public Identifier getPingBaseFSMId();
  }

  private static enum States implements FSMStateName {
    WAIT_BASIC_PONG, WAIT_PATTERN_PONG
  }

  public static class ExternalState implements FSMExternalState {

    ComponentProxy proxy;
    Positive<PingPort> pingPort;
    Negative<PingCtrlPort> pingCtrlPort;

    @Override
    public void setProxy(ComponentProxy proxy) {
      this.proxy = proxy;
      pingPort = proxy.getNegative(PingPort.class).getPair();
      pingCtrlPort = proxy.getPositive(PingCtrlPort.class).getPair();
    }

    @Override
    public ComponentProxy getProxy() {
      return proxy;
    }
  }

  private static class InternalState implements FSMInternalState {

    final FSMIdentifier fsmId;
    Identifier pongBaseFSMId;

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

    static FSMBasicEventHandler handleStart
      = new FSMBasicEventHandler<ExternalState, InternalState, StartPingEvent>() {
        @Override
        public FSMStateName handle(FSMStateName state, ExternalState es, InternalState is,
          StartPingEvent event) throws FSMException {
          LOG.info("{}", event);
          is.pongBaseFSMId = event.pongBaseFSMId;
          es.getProxy().trigger(new BasicEvent.Ping(is.fsmId.baseId, is.pongBaseFSMId), es.pingPort);
          return States.WAIT_BASIC_PONG;
        }
      };

    static FSMBasicEventHandler handleBasicPong
      = new FSMBasicEventHandler<ExternalState, InternalState, BasicEvent.Pong>() {
        @Override
        public FSMStateName handle(FSMStateName state, ExternalState es, InternalState is,
          BasicEvent.Pong pong) throws FSMException {
          LOG.trace("basic {}", pong);
          es.getProxy().trigger(new PatternEvent(new BasicEvent.Ping(is.fsmId.baseId, is.pongBaseFSMId)), es.pingPort);
          return States.WAIT_PATTERN_PONG;
        }
      };
    
    static FSMPatternEventHandler handlePatternPong
      = new FSMPatternEventHandler<ExternalState, InternalState, BasicEvent.Pong>() {
        @Override
        public FSMStateName handle(FSMStateName state, ExternalState es, InternalState is, BasicEvent.Pong pong,
          PatternExtractor<Class, BasicEvent.Pong> container) throws FSMException {
          LOG.trace("pattern {}", pong);
          es.getProxy().trigger(new BasicEvent.Ping(is.fsmId.baseId, is.pongBaseFSMId), es.pingPort);
          return States.WAIT_BASIC_PONG;
        }
      };
  }
}
