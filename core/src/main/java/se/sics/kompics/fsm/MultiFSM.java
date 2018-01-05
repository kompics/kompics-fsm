/*
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * KompicsToolbox is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.kompics.fsm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentProxy;
import se.sics.kompics.Handler;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.fsm.id.FSMIdentifier;
import se.sics.kompics.util.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class MultiFSM {

  private static final Logger LOG = LoggerFactory.getLogger(MultiFSM.class);
  private static final String FSM_NAME = "fsmName";
  private static final String FSM_ID = "fsmId";
  private static final String FSM_STATE = "fsmState";

  private final FSMachineDef fsmDef;
  private final OnFSMExceptionAction oexa;
  private final BaseIdExtractor fsmIdExtractor;
  private final Map<FSMIdentifier, FSMachine> fsms = new HashMap<>();
  private final FSMExternalState es;
  private final FSMInternalStateBuilder isb;
  private final Map<Class, Set<Class>> positiveBasicEvents;
  private final Map<Class, Set<Class>> negativeBasicEvents;
  private final Map<Class, Set<Pair<Class, Class>>> positivePatternEvents;
  private final Map<Class, Set<Pair<Class, Class>>> negativePatternEvents;

  private final FSMOnKillAction oka = new FSMOnKillAction() {
    @Override
    public void kill(FSMIdentifier fsmId) {
      MDC.put(FSM_NAME, fsmDef.fsmName);
      MDC.put(FSM_ID, fsmId.toString());
      try {
        LOG.info("removing fsm");
        fsms.remove(fsmId);
      } finally {
        MDC.remove(FSM_NAME);
        MDC.remove(FSM_ID);
      }
    }
  };

  private Optional<FSMachine> getFSM(KompicsEvent event) throws FSMException {
    Optional<Identifier> baseId = fsmIdExtractor.fromEvent(event);
    if (!baseId.isPresent()) {
      LOG.warn("not handling event:{}", event);
      return Optional.empty();
    }
    FSMIdentifier fsmId = fsmDef.getFsmId(baseId.get());
    FSMachine fsm = fsms.get(fsmId);
    if (fsm == null) {
      fsm = fsmDef.build(fsmId.baseId, oka, es, isb.newState(fsmId));
      fsms.put(fsmId, fsm);
    }
    return Optional.of(fsm);
  }

  private <E extends FSMEvent> Handler<E> basicEventOnPositivePort(Class<E> eventType) {

    return new Handler<E>(eventType) {
      @Override
      public void handle(E event) {
        MDC.put(FSM_NAME, fsmDef.fsmName);
        try {
          Optional<FSMachine> fsm = getFSM(event);
          if (fsm.isPresent()) {
            MDC.put(FSM_ID, fsm.get().fsmId.toString());
            MDC.put(FSM_STATE, fsm.get().currentState.getValue0().toString());
            fsm.get().handlePositive(event);
          }
        } catch (FSMException ex) {
          oexa.handle(ex);
        } finally {
          MDC.remove(FSM_NAME);
          MDC.remove(FSM_ID);
          MDC.remove(FSM_STATE);
        }
      }
    };
  }

  private <E extends FSMEvent> Handler<E> basicEventOnNegativePort(Class<E> eventType) {
    return new Handler<E>(eventType) {
      @Override
      public void handle(E event) {
        try {
          MDC.put(FSM_NAME, fsmDef.fsmName);
          Optional<FSMachine> fsm = getFSM(event);
          if (fsm.isPresent()) {
            MDC.put(FSM_ID, fsm.get().fsmId.toString());
            MDC.put(FSM_STATE, fsm.get().currentState.getValue0().toString());
            fsm.get().handleNegative(event);
          }
        } catch (FSMException ex) {
          oexa.handle(ex);
        } finally {
          MDC.remove(FSM_NAME);
          MDC.remove(FSM_ID);
          MDC.remove(FSM_STATE);
        }
      }
    };
  }

  private <P extends KompicsEvent, C extends PatternExtractor<Class<Object>, P>> 
  ClassMatchedHandler patternEventOnPositivePort(Class contentType, Class<C> containerType) {
    return new ClassMatchedHandler<P, C>(containerType, contentType) {
      @Override
      public void handle(P payload, C container) {
        MDC.put(FSM_NAME, fsmDef.fsmName);
        try {
          
          Optional<FSMachine> fsm = getFSM(container);
          if (fsm.isPresent()) {
            MDC.put(FSM_ID, fsm.get().fsmId.toString());
            MDC.put(FSM_STATE, fsm.get().currentState.getValue0().toString());
            fsm.get().handlePositive(payload, container);
          }
        } catch (FSMException ex) {
          oexa.handle(ex);
        } finally {
          MDC.remove(FSM_NAME);
          MDC.remove(FSM_ID);
          MDC.remove(FSM_STATE);
        }
      }
    };
  }

  private <P extends FSMEvent, C extends PatternExtractor<Class<Object>, P>> ClassMatchedHandler patternEventOnNegativePort(Class contentType, Class<C> containerType) {
    return new ClassMatchedHandler<P, C>(containerType, contentType) {
      @Override
      public void handle(P payload, C container) {
        MDC.put(FSM_NAME, fsmDef.fsmName);
        try {
          Optional<FSMachine> fsm = getFSM(payload);
          if (fsm.isPresent()) {
            MDC.put(FSM_ID, fsm.get().fsmId.toString());
            MDC.put(FSM_STATE, fsm.get().currentState.getValue0().toString());
            fsm.get().handleNegative(payload, container);
          }
        } catch (FSMException ex) {
          oexa.handle(ex);
        } finally {
          MDC.remove(FSM_NAME);
          MDC.remove(FSM_ID);
          MDC.remove(FSM_STATE);
        }
      }
    };
  }

  // Class1 - ? extends PortType , Class2 - ? extends FSMEvent(KompicsEvent)
  public MultiFSM(FSMachineDef fsmDef, OnFSMExceptionAction oexa, BaseIdExtractor fsmIdExtractor,
    FSMExternalState es, FSMInternalStateBuilder isb,
    Map<Class, Set<Class>> positiveBasicEvents, Map<Class, Set<Class>> negativeBasicEvents,
    Map<Class, Set<Pair<Class,Class>>> positivePatternEvents, Map<Class, Set<Pair<Class, Class>>> negativePatternEvents) {
    this.fsmDef = fsmDef;
    this.oexa = oexa;
    this.fsmIdExtractor = fsmIdExtractor;
    this.es = es;
    this.isb = isb;
    this.positiveBasicEvents = positiveBasicEvents;
    this.negativeBasicEvents = negativeBasicEvents;
    this.positivePatternEvents = positivePatternEvents;
    this.negativePatternEvents = negativePatternEvents;
  }

  void setProxy(ComponentProxy proxy) {
    this.es.setProxy(proxy);
  }

  public void setupHandlers() {
    Pair<List, List> basicEvents = prepareBasicEvents();
    Pair<List, List> patternEvents = preparePatternEvents();
    GenericSetup.handledEvents(LOG, es.getProxy(), basicEvents.getValue0(), basicEvents.getValue1(),
      patternEvents.getValue0(), patternEvents.getValue1());
  }

  private Pair<List, List> prepareBasicEvents() {
    List pPorts = new LinkedList<>();
    List nPorts = new LinkedList<>();

    for (Map.Entry<Class, Set<Class>> e : positiveBasicEvents.entrySet()) {
      List<Handler> events = new LinkedList<>();
      for (Class c : e.getValue()) {
        events.add(basicEventOnPositivePort(c));
      }
      pPorts.add(Pair.with(e.getKey(), events));
    }
    for (Map.Entry<Class, Set<Class>> e : negativeBasicEvents.entrySet()) {
      List<Handler> events = new LinkedList<>();
      for (Class c : e.getValue()) {
        events.add(basicEventOnNegativePort(c));
      }
      nPorts.add(Pair.with(e.getKey(), events));
    }
    return Pair.with(pPorts, nPorts);
  }

  private Pair<List, List> preparePatternEvents() {
    List pPorts = new LinkedList<>();
    List nPorts = new LinkedList<>();
    for (Map.Entry<Class, Set<Pair<Class, Class>>> e : positivePatternEvents.entrySet()) {
      List<ClassMatchedHandler> events = new LinkedList<>();
      for (Pair<Class, Class> c : e.getValue()) {
        events.add(patternEventOnPositivePort(c.getValue0(), c.getValue1()));
      }
      pPorts.add(Pair.with(e.getKey(), events));
    }
    for (Map.Entry<Class, Set<Pair<Class, Class>>> e : negativePatternEvents.entrySet()) {
      List<ClassMatchedHandler> events = new LinkedList<>();
      for (Pair<Class, Class> c : e.getValue()) {
        events.add(patternEventOnNegativePort(c.getValue0(), c.getValue1()));
      }
      nPorts.add(Pair.with(e.getKey(), events));
    }
    return Pair.with(pPorts, nPorts);
  }

  public boolean activeFSM(Identifier baseId) {
    FSMIdentifier fsmId = fsmDef.getFsmId(baseId);
    return fsms.containsKey(fsmId);
  }
  
  public FSMStateName getFSMState(Identifier baseId) {
    FSMIdentifier fsmId = fsmDef.getFsmId(baseId);
    FSMachine fsm = fsms.get(fsmId);
    return fsm.getState();
  }

  public boolean isEmpty() {
    return fsms.isEmpty();
  }

  public int size() {
    return fsms.size();
  }
  
  //*********************************************TESTING_HELPERS********************************************************
  public FSMInternalState getFSMInternalState(Identifier baseId) {
    FSMIdentifier fsmId = fsmDef.getFsmId(baseId);
    FSMachine fsm = fsms.get(fsmId);
    return fsm.getFSMInternalState();
  }
}
