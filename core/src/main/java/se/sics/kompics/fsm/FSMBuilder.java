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

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.javatuples.Pair;
import se.sics.kompics.fsm.handler.FSMBasicEventHandler;
import se.sics.kompics.fsm.handler.FSMPatternEventHandler;
import se.sics.kompics.fsm.handler.FSMStateChangeHandler;
import se.sics.kompics.fsm.id.FSMIdentifierFactory;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class FSMBuilder {

  public static class MultiMachine {

    private Map<Class, Set<Class>> positiveBasicEvents = new HashMap<>();
    private Map<Class, Set<Class>> negativeBasicEvents = new HashMap<>();
    private final Set<Class> basicEvents = new HashSet<>();
    private Map<Class, Set<Pair<Class, Class>>> positivePatternEvents = new HashMap<>();
    private Map<Class, Set<Pair<Class, Class>>> negativePatternEvents = new HashMap<>();
    private final Set<Pair<Class, Class>> patternEvents = new HashSet<>();

    private MultiMachine() {
    }

    public MultiMachine setPositiveBasicEvents(Map<Class, Set<Class>> events) {
      positiveBasicEvents = events;
      for (Set<Class> e : events.values()) {
        basicEvents.addAll(e);
      }
      return this;
    }

    public MultiMachine setNegativeBasicEvents(Map<Class, Set<Class>> events) {
      negativeBasicEvents = events;
      for (Set<Class> e : events.values()) {
        basicEvents.addAll(e);
      }
      return this;
    }

    public MultiMachine setPositivePatternEvents(Map<Class, Set<Pair<Class, Class>>> events) {
      this.positivePatternEvents = events;
      for (Set<Pair<Class, Class>> e : events.values()) {
        patternEvents.addAll(e);
      }
      return this;
    }

    public MultiMachine setNegativePatternEvents(Map<Class, Set<Pair<Class, Class>>> events) {
      this.negativePatternEvents = events;
      for (Set<Pair<Class, Class>> e : events.values()) {
        patternEvents.addAll(e);
      }
      return this;
    }

    public MultiFSM buildMultiFSM(final FSMachineDef fsmDef, OnFSMExceptionAction oexa, FSMExternalState es,
      FSMInternalStateBuilder builder, BaseIdExtractor fsmIdExtractor) throws FSMException {

      MultiFSM multiFSM = new MultiFSM(fsmDef, oexa, fsmIdExtractor, es, builder, positiveBasicEvents, negativeBasicEvents,
        positivePatternEvents, negativePatternEvents);
      return multiFSM;
    }

    public static MultiMachine instance() {
      return new MultiMachine();
    }
  }

  public static class StructuralDefinition {

    private final Table<FSMStateName, FSMStateName, Boolean> transitionTable = HashBasedTable.create();
    private final Map<FSMStateName, FSMStateDef> states = new HashMap<>();

    private StructuralDefinition() {
    }

    public Transition onStart() throws FSMException {
      return onState(FSMBasicStateNames.START);
    }
    
    public Transition onState(FSMStateName state) throws FSMException {
      if (transitionTable.containsRow(state)) {
        throw new FSMException("state:" + state + " already registered");
      }
      return new Transition(this, state);
    }

    private void buildTransition(FSMStateName from, FSMStateName[] toStates, boolean toFinal,
      Optional<FSMStateChangeHandler> onEntry, Optional<FSMStateChangeHandler> onExit)
      throws FSMException {

      for (FSMStateName to : toStates) {
        if (transitionTable.contains(from, to)) {
          throw new FSMException("transition from:" + from + " to:" + to + " already registered");
        }
        transitionTable.put(from, to, true);
      }
      if (toFinal) {
        transitionTable.put(from, FSMBasicStateNames.FINAL, true);
      }

      if (states.containsKey(from)) {
        throw new FSMException("state:" + from + "already defined");
      }
      FSMStateDef stateDef = FSMStateDef.instance()
        .setOnEntry(onEntry)
        .setOnExit(onExit);
      states.put(from, stateDef);
    }
  }

  public static class Transition {

    private final StructuralDefinition parent;
    private final FSMStateName from;
    private FSMStateName[] toStates = new FSMStateName[0];
    private boolean toFinal = false;

    private Optional<FSMStateChangeHandler> onEntry = Optional.absent();
    private Optional<FSMStateChangeHandler> onExit = Optional.absent();

    private Transition(StructuralDefinition parent, FSMStateName from) {
      this.parent = parent;
      this.from = from;
    }

    public Transition onEntry(FSMStateChangeHandler handler) {
      onEntry = Optional.of(handler);
      return this;
    }

    public Transition onExit(FSMStateChangeHandler handler) {
      onExit = Optional.of(handler);
      return this;
    }

    public Transition nextStates(FSMStateName... ids) {
      this.toStates = ids;
      return this;
    }

    public Transition toFinal() {
      this.toFinal = true;
      return this;
    }

    public StructuralDefinition buildTransition() throws FSMException {
      if (toStates == null) {
        throw new FSMException("to states not registered");
      }
      parent.buildTransition(from, toStates, toFinal, onEntry, onExit);
      return parent;
    }
  }

  public static class SemanticDefinition {

    private final Map<Class, Set<Class>> negativeBasicEvents = new HashMap<>();
    private final Map<Class, Set<Class>> positiveBasicEvents = new HashMap<>();
    private final Map<Class, Set<Pair<Class, Class>>> positivePatternEvents = new HashMap<>();
    private final Map<Class, Set<Pair<Class, Class>>> negativePatternEvents = new HashMap<>();

    private FSMBasicEventHandler defaultFallbackBasicEvent = FSMachine.defaultFallbackBasicEvent;
    private FSMPatternEventHandler defaultFallbackPatternEvent = FSMachine.defaultFallbackPatternEvent;
    private final Table<Class, FSMStateName, FSMBasicEventHandler> positiveBasicEventHandlers = HashBasedTable.create();
    private final Map<Class, FSMBasicEventHandler> positiveBasicFallbackHandlers = new HashMap<>();
    private final Table<Class, FSMStateName, FSMBasicEventHandler> negativeBasicEventHandlers = HashBasedTable.create();
    private final Map<Class, FSMBasicEventHandler> negativeBasicFallback = new HashMap<>();
    private final Table<Pair<Class, Class>, FSMStateName, FSMPatternEventHandler> positivePatternEventHandlers = HashBasedTable.create();
    private final Map<Pair<Class, Class>, FSMPatternEventHandler> positivePatternFallback = new HashMap<>();
    private final Table<Pair<Class, Class>, FSMStateName, FSMPatternEventHandler> negativePatternEventHandlers = HashBasedTable.create();
    private final Map<Pair<Class, Class>, FSMPatternEventHandler> negativePatternFallback = new HashMap<>();

    public SemanticDefinition() {
    }

    public Port positivePort(Class portType) {
      return new Port(this, portType, true);
    }

    public Port negativePort(Class portType) {
      return new Port(this, portType, false);
    }
    
    public SemanticDefinition defaultFallback(FSMBasicEventHandler eventHandler, FSMPatternEventHandler msgHandler) {
      defaultFallbackBasicEvent = eventHandler;
      defaultFallbackPatternEvent = msgHandler;
      return this;
    }

    private void buildPort(Class portType, boolean pp, 
      Map<Class, Map<FSMStateName, FSMBasicEventHandler>> basicHandlers,
      Map<Pair<Class, Class>, Map<FSMStateName, FSMPatternEventHandler>> patternHandlers, 
      Map<Class, FSMBasicEventHandler> basicFallback, 
      Map<Pair<Class, Class>, FSMPatternEventHandler> patternFallback) throws FSMException {
      if (pp) {
        positiveBasicEvents.put(portType, basicHandlers.keySet());
        for (Map.Entry<Class, Map<FSMStateName, FSMBasicEventHandler>> e : basicHandlers.entrySet()) {
          if (positiveBasicEventHandlers.containsRow(e.getKey())) {
            throw new FSMException("currently we do not allow same event in different ports(besides pos/neg");
          }
          for (Map.Entry<FSMStateName, FSMBasicEventHandler> ee : e.getValue().entrySet()) {
            positiveBasicEventHandlers.put(e.getKey(), ee.getKey(), ee.getValue());
          }
        }
        positiveBasicFallbackHandlers.putAll(basicFallback);
        
        positivePatternEvents.put(portType, patternHandlers.keySet());
        for (Map.Entry<Pair<Class, Class>, Map<FSMStateName, FSMPatternEventHandler>> e : patternHandlers.entrySet()) {
          if (positivePatternEventHandlers.containsRow(e.getKey())) {
            throw new FSMException("currently we do not allow same event in different ports(besides pos/neg");
          }
          for (Map.Entry<FSMStateName, FSMPatternEventHandler> ee : e.getValue().entrySet()) {
            positivePatternEventHandlers.put(e.getKey(), ee.getKey(), ee.getValue());
          }
        }
        positivePatternFallback.putAll(patternFallback);
      } else {
        negativeBasicEvents.put(portType, basicHandlers.keySet());
        for (Map.Entry<Class, Map<FSMStateName, FSMBasicEventHandler>> e : basicHandlers.entrySet()) {
          if (negativeBasicEventHandlers.containsRow(e.getKey())) {
            throw new FSMException("currently we do not allow same event in different ports(besides pos/neg");
          }
          for (Map.Entry<FSMStateName, FSMBasicEventHandler> ee : e.getValue().entrySet()) {
            negativeBasicEventHandlers.put(e.getKey(), ee.getKey(), ee.getValue());
          }
        }
        negativeBasicFallback.putAll(basicFallback);
        
        negativePatternEvents.put(portType, patternHandlers.keySet());
        for (Map.Entry<Pair<Class, Class>, Map<FSMStateName, FSMPatternEventHandler>> e : patternHandlers.entrySet()) {
          if (negativePatternEventHandlers.containsRow(e.getKey())) {
            throw new FSMException("currently we do not allow same event in different ports(besides pos/neg");
          }
          for (Map.Entry<FSMStateName, FSMPatternEventHandler> ee : e.getValue().entrySet()) {
            negativePatternEventHandlers.put(e.getKey(), ee.getKey(), ee.getValue());
          }
        }
        negativePatternFallback.putAll(patternFallback);
      }
    }
  }

  public static class Port {

    private final SemanticDefinition parent;
    private final Class portType;
    private final boolean pp;

    private final Map<Class, Map<FSMStateName, FSMBasicEventHandler>> basicHandlers = new HashMap<>();
    private final Map<Class, FSMBasicEventHandler> basicFallback = new HashMap<>();
    private final Map<Pair<Class, Class>, Map<FSMStateName, FSMPatternEventHandler>> patternHandlers = new HashMap<>();
    private final Map<Pair<Class, Class>, FSMPatternEventHandler> patternFallback = new HashMap<>();

    public Port(SemanticDefinition parent, Class portType, boolean pp) {
      this.parent = parent;
      this.portType = portType;
      this.pp = pp;
    }

    public BasicEvent onBasicEvent(Class eventType) {
      return new BasicEvent(this, eventType);
    }
    
    public PatternEvent onPatternEvent(Class contentType, Class containerType) {
      return new PatternEvent(this, contentType, containerType);
    }

    private void buildBasicEvent(Class eventType, Map<FSMStateName, FSMBasicEventHandler> handlers,
      Optional<FSMBasicEventHandler> fallback) {
      basicHandlers.put(eventType, handlers);
      if (fallback.isPresent()) {
        basicFallback.put(eventType, fallback.get());
      }
    }
    
    private void buildPatternEvent(Class contentType, Class containerType, Map<FSMStateName, FSMPatternEventHandler> handlers,
      Optional<FSMPatternEventHandler> fallback) {
      patternHandlers.put(Pair.with(contentType, containerType), handlers);
      if (fallback.isPresent()) {
        patternFallback.put(Pair.with(contentType, containerType), fallback.get());
      }
    }

    public SemanticDefinition buildPort() throws FSMException {
      parent.buildPort(portType, pp, basicHandlers, patternHandlers, basicFallback, patternFallback);
      return parent;
    }
  }

  public static class BasicEvent {

    private final Port parent;
    private final Class eventType;
    private final Map<FSMStateName, FSMBasicEventHandler> handlers = new HashMap<>();
    private Optional<FSMBasicEventHandler> fallback = Optional.absent();

    private BasicEvent(Port parent, Class eventType) {
      this.parent = parent;
      this.eventType = eventType;
    }
    
    public BasicEvent subscribeOnStart(FSMBasicEventHandler handler) throws FSMException {
      return subscribe(handler, FSMBasicStateNames.START);
    }

    public BasicEvent subscribe(FSMBasicEventHandler handler, FSMStateName... states) throws FSMException {
      for (FSMStateName state : states) {
        if (handlers.containsKey(state)) {
          throw new FSMException("handler already registered for state:" + state + " event:" + eventType);
        }
        handlers.put(state, handler);
      }
      return this;
    }

    public BasicEvent fallback(FSMBasicEventHandler handler) {
      fallback = Optional.of(handler);
      return this;
    }

    public Port buildEvent() {
      parent.buildBasicEvent(eventType, handlers, fallback);
      return parent;
    }

    //*********SHORTCUTS**********
    public BasicEvent onBasicEvent(Class eventType) {
      return buildEvent().onBasicEvent(eventType);
    }
    
    public PatternEvent onPatternEvent(Class contentType, Class containerType) {
      return buildEvent().onPatternEvent(contentType, containerType);
    }

    public SemanticDefinition buildEvents() throws FSMException {
      parent.buildBasicEvent(eventType, handlers, fallback);
      return parent.buildPort();
    }
  }

  public static class PatternEvent {

    private final Port parent;
    private final Class contentType;
    private final Class containerType;
    private final Map<FSMStateName, FSMPatternEventHandler> handlers = new HashMap<>();
    private Optional<FSMPatternEventHandler> fallback = Optional.absent();

    private PatternEvent(Port parent, Class contentType, Class containerType) {
      this.parent = parent;
      this.contentType = contentType;
      this.containerType = containerType;
    }

    public PatternEvent subscribe(FSMPatternEventHandler handler, FSMStateName... states) throws FSMException {
      for (FSMStateName state : states) {
        if (handlers.containsKey(state)) {
          throw new FSMException("handler already registered for state:" + state + " event:" + contentType);
        }
        handlers.put(state, handler);
      }
      return this;
    }

    public PatternEvent fallback(FSMPatternEventHandler handler) {
      fallback = Optional.of(handler);
      return this;
    }

    public Port buildEvent() {
      parent.buildPatternEvent(contentType, containerType, handlers, fallback);
      return parent;
    }

    //*********SHORTCUTS**********
    public BasicEvent onBasicEvent(Class eventType) {
      return buildEvent().onBasicEvent(eventType);
    }
    
    public PatternEvent onPatternEvent(Class contentType, Class containerType) {
      return buildEvent().onPatternEvent(contentType, containerType);
    }

    public SemanticDefinition buildEvents() throws FSMException {
      parent.buildPatternEvent(contentType, containerType, handlers, fallback);
      return parent.buildPort();
    }
  }

  public static StructuralDefinition structuralDef() {
    return new StructuralDefinition();
  }

  public static SemanticDefinition semanticDef() {
    return new SemanticDefinition();
  }

  private static FSMachineDef fsmDef(FSMIdentifierFactory fsmIdFactory, String fsmName, StructuralDefinition m, SemanticDefinition h) throws FSMException {
    if (!m.transitionTable.containsRow(FSMBasicStateNames.START)) {
      throw new FSMException("START state not defined");
    }
    if (!m.transitionTable.containsColumn(FSMBasicStateNames.FINAL)) {
      throw new FSMException("FINAL state not defined");
    }
    Sets.SetView<FSMStateName> deadState = Sets.
      difference(m.transitionTable.columnKeySet(), m.transitionTable.rowKeySet());
    if (deadState.size() > 1) {
      throw new FSMException("states:" + deadState.toString()
        + "are dead end states. Only FINAL allowed as dead end state.");
    }

    for (Map.Entry<FSMStateName, FSMStateDef> stateDef : m.states.entrySet()) {
      stateDef.getValue().setNegativeBasicHandlers(h.negativeBasicEventHandlers.column(stateDef.getKey()));
      stateDef.getValue().setPositiveBasicHandlers(h.positiveBasicEventHandlers.column(stateDef.getKey()));
      stateDef.getValue().setNegativePatternHandlers(h.negativePatternEventHandlers.column(stateDef.getKey()));
      stateDef.getValue().setPositivePatternHandlers(h.positivePatternEventHandlers.column(stateDef.getKey()));
    }

    FSMachineDef fsmDef = FSMachineDef.definition(fsmIdFactory, fsmName, m.states, m.transitionTable,
      h.defaultFallbackBasicEvent, h.defaultFallbackPatternEvent,
      h.positiveBasicFallbackHandlers, h.negativeBasicFallback,
      h.positivePatternFallback, h.negativePatternFallback);

    return fsmDef;
  }

  public static MultiFSM multiFSM(FSMIdentifierFactory fsmIdFactory, String fsmName, StructuralDefinition m, 
    SemanticDefinition h, FSMExternalState es,
    FSMInternalStateBuilder isb, OnFSMExceptionAction oexa, BaseIdExtractor fsmIdExtractor) throws FSMException {

    FSMachineDef fsmDef = fsmDef(fsmIdFactory, fsmName, m, h);

    MultiFSM multiFSM = MultiMachine.instance()
      .setPositiveBasicEvents(h.positiveBasicEvents)
      .setNegativeBasicEvents(h.negativeBasicEvents)
      .setPositivePatternEvents(h.positivePatternEvents)
      .setNegativePatternEvents(h.negativePatternEvents)
      .buildMultiFSM(fsmDef, oexa, es, isb, fsmIdExtractor);
    return multiFSM;
  }

}
