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

import com.google.common.collect.Table;
import java.util.Map;
import java.util.Optional;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.fsm.handler.FSMBasicEventHandler;
import se.sics.kompics.fsm.handler.FSMPatternEventHandler;
import se.sics.kompics.fsm.id.FSMIdentifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class FSMachine {

  private final static Logger LOG = LoggerFactory.getLogger(FSMachine.class);

  public final FSMIdentifier fsmId;
  private final FSMOnKillAction oka;
  Pair<FSMStateName, FSMState> currentState;
  private final Map<FSMStateName, FSMState> states;
  private final Table<FSMStateName, FSMStateName, Boolean> transitionTable;
  private final FSMBasicEventHandler defaultFallbackBasicEvents;
  private final FSMPatternEventHandler defaultFallbackPatternEvents;
  private final Map<Class, FSMBasicEventHandler> fallbackPositiveBasicEvents;
  private final Map<Class, FSMBasicEventHandler> fallbackNegativeBasicEvents;
  private final Map<Pair<Class, Class>, FSMPatternEventHandler> fallbackPositivePatternEvents;
  private final Map<Pair<Class, Class>, FSMPatternEventHandler> fallbackNegativePatternEvents;

  public FSMachine(FSMIdentifier fsmId, FSMOnKillAction oka, Map<FSMStateName, FSMState> states,
    Table<FSMStateName, FSMStateName, Boolean> transitionTable,
    FSMBasicEventHandler defaultFallbackBasicEvents, FSMPatternEventHandler defaultFallbackPatternEvents,
    Map<Class, FSMBasicEventHandler> fallbackPositiveBasicEvents, 
    Map<Class, FSMBasicEventHandler> fallbackNegativeBasicEvents,
    Map<Pair<Class, Class>, FSMPatternEventHandler> fallbackPositivePatternEvents, 
    Map<Pair<Class, Class>, FSMPatternEventHandler> fallbackNegativePatternEvents) {
    this.fsmId = fsmId;
    this.oka = oka;
    this.states = states;
    this.transitionTable = transitionTable;
    this.defaultFallbackBasicEvents = defaultFallbackBasicEvents;
    this.defaultFallbackPatternEvents = defaultFallbackPatternEvents;
    this.currentState = Pair.with((FSMStateName) FSMBasicStateNames.START, states.get(FSMBasicStateNames.START));
    this.fallbackPositiveBasicEvents = fallbackPositiveBasicEvents;
    this.fallbackNegativeBasicEvents = fallbackNegativeBasicEvents;
    this.fallbackPositivePatternEvents = fallbackPositivePatternEvents;
    this.fallbackNegativePatternEvents = fallbackNegativePatternEvents;
  }

  public void handlePositive(KompicsEvent event) throws FSMException {
    LOG.trace("handle event:{}", event);
    Optional<FSMStateName> next = currentState.getValue1().handlePositive(event);
    if (!next.isPresent()) {
      FSMBasicEventHandler fallback = fallbackPositiveBasicEvents.get(event.getClass());
      if (fallback == null) {
        LOG.info("not handling positive port event:{}", event);
        return;
      } else {
        next = Optional.of(currentState.getValue1().fallback(event, fallback));
      }
    }
    handle(next.get(), event);
  }

  public void handleNegative(KompicsEvent event) throws FSMException {
    LOG.trace("handle event:{}", event);
    Optional<FSMStateName> next = currentState.getValue1().handleNegative(event);
    if (!next.isPresent()) {
      FSMBasicEventHandler fallback = fallbackNegativeBasicEvents.get(event.getClass());
      if (fallback == null) {
        LOG.info("not handling negative port event:{}", event);
        return;
      } else {
        next = Optional.of(currentState.getValue1().fallback(event, fallback));
      }
    }
    handle(next.get(), event);
  }

  private void handle(FSMStateName next, KompicsEvent event) throws FSMException {
    if (FSMBasicStateNames.FINAL.equals(next)) {
      oka.kill(fsmId);
      return;
    }
    //we can't check at definition the sanity or completion of transition table
    if (!transitionTable.contains(currentState.getValue0(), next)) {
      throw new FSMException("transition from:" + currentState.getValue0() + " to:" + next + " not defined");
    }
    LOG.trace("event:{} resulted in transition to state:{}", event, next);
    //we check at definition that both from and to states of a transition are registered
    currentState = Pair.with(next, states.get(next));
  }

  public void handlePositive(KompicsEvent payload, PatternExtractor container) throws
    FSMException {
    LOG.trace("handle container:{}", container);
    Optional<FSMStateName> next = currentState.getValue1().handlePositive(payload, container);
    if (!next.isPresent()) {
      FSMPatternEventHandler fallback = fallbackPositivePatternEvents.get(Pair.with(payload.getClass(), container.getClass()));
      if (fallback == null) {
        LOG.info("not handling positive container:{}", container);
        return;
      } else {
        next = Optional.of(currentState.getValue1().fallback(payload, container, fallback));
      }
    }
    handle(next.get(), payload, container);
  }

  public void handleNegative(KompicsEvent payload, PatternExtractor container) throws
    FSMException {
    LOG.trace("handle container:{}", container);
    Optional<FSMStateName> next = currentState.getValue1().handleNegative(payload, container);
    if (!next.isPresent()) {
      FSMPatternEventHandler fallback = fallbackNegativePatternEvents.get(Pair.with(payload.getClass(), container.getClass()));
      if (fallback == null) {
        LOG.info("not handling negative container:{}", container);
        return;
      } else {
        next = Optional.of(currentState.getValue1().fallback(payload, container, fallback));
      }
    }
    handle(next.get(), payload, container);
  }

  private void handle(FSMStateName next, KompicsEvent payload, PatternExtractor<Class, KompicsEvent> container)
    throws FSMException {
    if (FSMBasicStateNames.FINAL.equals(next)) {
      oka.kill(fsmId);
      return;
    }
    //we can't check at definition the sanity or completion of transition table
    if (!transitionTable.contains(currentState.getValue0(), next)) {
      throw new FSMException("transition from:" + currentState.getValue0() + " to:" + next + " not defined");
    }
    LOG.trace("container:{} resulted in transition to state:{}", container, next);
    //we check at definition that both from and to states of a transition are registered
    currentState = Pair.with(next, states.get(next));
  }
  
  static FSMBasicEventHandler defaultFallbackBasicEvent = new FSMBasicEventHandler<FSMExternalState, FSMInternalState, FSMEvent>() {
    @Override
    public FSMStateName handle(FSMStateName state, FSMExternalState es, FSMInternalState is, FSMEvent event) 
      throws FSMException {
      LOG.info("not handling event:{}", event);
      return state;
    }
  };
  
  static FSMPatternEventHandler defaultFallbackPatternEvent = new FSMPatternEventHandler<FSMExternalState, FSMInternalState, FSMEvent>() {
    @Override
     public FSMStateName handle(FSMStateName state, FSMExternalState es, FSMInternalState is, FSMEvent payload,
      PatternExtractor<Class, FSMEvent> container) throws FSMException {
      LOG.info("not handling container:{}", container);
      return state;
    }
  };
  
  public FSMStateName getState() {
    return currentState.getValue0();
  }
  
  //*********************************************TESTING_HELPERS********************************************************
  public FSMInternalState getFSMInternalState() {
    return currentState.getValue1().getFSMInternalState();
  }
}
