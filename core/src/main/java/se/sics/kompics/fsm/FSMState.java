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

import java.util.Map;
import java.util.Optional;
import org.javatuples.Pair;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.fsm.handler.FSMBasicEventHandler;
import se.sics.kompics.fsm.handler.FSMPatternEventHandler;
import se.sics.kompics.fsm.handler.FSMStateChangeHandler;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class FSMState {
  
  private final FSMStateName state;
  private final Optional<FSMStateChangeHandler> onEntry;
  private final Optional<FSMStateChangeHandler> onExit;
  private final FSMExternalState es;
  private final FSMInternalState is;
  
  private final Map<Class, FSMBasicEventHandler> positiveBasicHandlers;
  private final Map<Class, FSMBasicEventHandler> negativeBasicHandlers;
  
  private final Map<Pair<Class, Class>, FSMPatternEventHandler> positivePatternHandlers;
  private final Map<Pair<Class, Class>, FSMPatternEventHandler> negativePatternHandlers;
  
  public FSMState(FSMStateName state, Optional<FSMStateChangeHandler> onEntry, Optional<FSMStateChangeHandler> onExit,
    FSMExternalState es, FSMInternalState is,
    Map<Class, FSMBasicEventHandler> positiveBasicHandlers, Map<Class, FSMBasicEventHandler> negativeBasicHandlers,
    Map<Pair<Class, Class>, FSMPatternEventHandler> positivePatternHandlers, 
    Map<Pair<Class, Class>, FSMPatternEventHandler> negativePatternHandlers) {
    this.state = state;
    this.onEntry = onEntry;
    this.onExit = onExit;
    this.es = es;
    this.is = is;
    this.positiveBasicHandlers = positiveBasicHandlers;
    this.negativeBasicHandlers = negativeBasicHandlers;
    this.positivePatternHandlers = positivePatternHandlers;
    this.negativePatternHandlers = negativePatternHandlers;
  }
  
  public void onEntry(FSMStateName from) {
    if (onEntry.isPresent()) {
      onEntry.get().handle(from, state, es, is);
    }
  }
  
  public void onExit(FSMStateName to) {
    if (onExit.isPresent()) {
      onExit.get().handle(state, to, es, is);
    }
  }
  
  public Optional<FSMStateName> handlePositive(KompicsEvent event) throws FSMException {
    FSMBasicEventHandler handler = positiveBasicHandlers.get(event.getClass());
    if (handler == null) {
      return Optional.empty();
    }
    FSMStateName next = handler.handle(state, es, is, event);
    return Optional.of(next);
  }
  
  public Optional<FSMStateName> handleNegative(KompicsEvent event) throws FSMException {
    FSMBasicEventHandler handler = negativeBasicHandlers.get(event.getClass());
    if (handler == null) {
      return Optional.empty();
    }
    FSMStateName next = handler.handle(state, es, is, event);
    return Optional.of(next);
  }
  
  public Optional<FSMStateName> handlePositive(KompicsEvent payload, PatternExtractor<Class, KompicsEvent> container)
    throws FSMException {
    FSMPatternEventHandler handler = positivePatternHandlers.get(Pair.with(payload.getClass(), container.getClass()));
    if (handler == null) {
      return Optional.empty();
    }
    FSMStateName next = handler.handle(state, es, is, payload, container);
    return Optional.of(next);
  }
  
  public Optional<FSMStateName> handleNegative(KompicsEvent payload, PatternExtractor<Class, KompicsEvent> container)
    throws FSMException {
    FSMPatternEventHandler handler = negativePatternHandlers.get(Pair.with(payload.getClass(), container.getClass()));
    if (handler == null) {
      return Optional.empty();
    }
    FSMStateName next = handler.handle(state, es, is, payload, container);
    return Optional.of(next);
  }
  
  public FSMStateName fallback(KompicsEvent event, FSMBasicEventHandler fallback) throws FSMException {
    return fallback.handle(state, es, is, event);
  }

  public FSMStateName fallback(KompicsEvent payload, PatternExtractor<Class, KompicsEvent> container, FSMPatternEventHandler fallback) throws FSMException {
    return fallback.handle(state, es, is, payload, container);
  }
  
  //*********************************************TESTING_HELPERS********************************************************
  public FSMInternalState getFSMInternalState() {
    return is;
  }
}
