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
import java.util.Map;
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
  
  private final Map<Class, FSMBasicEventHandler> positiveHandlers;
  private final Map<Class, FSMBasicEventHandler> negativeHandlers;
  
  private final Map<Class, FSMPatternEventHandler> positiveMsgHandlers;
  private final Map<Class, FSMPatternEventHandler> negativeMsgHandlers;
  
  public FSMState(FSMStateName state, Optional<FSMStateChangeHandler> onEntry, Optional<FSMStateChangeHandler> onExit,
    FSMExternalState es, FSMInternalState is,
    Map<Class, FSMBasicEventHandler> positiveHandlers, Map<Class, FSMBasicEventHandler> negativeHandlers,
    Map<Class, FSMPatternEventHandler> positiveMsgHandlers, Map<Class, FSMPatternEventHandler> negativeMsgHandlers) {
    this.state = state;
    this.onEntry = onEntry;
    this.onExit = onExit;
    this.es = es;
    this.is = is;
    this.positiveHandlers = positiveHandlers;
    this.negativeHandlers = negativeHandlers;
    this.positiveMsgHandlers = positiveMsgHandlers;
    this.negativeMsgHandlers = negativeMsgHandlers;
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
  
  public Optional<FSMStateName> handlePositive(FSMEvent event) throws FSMException {
    FSMBasicEventHandler handler = positiveHandlers.get(event.getClass());
    if (handler == null) {
      return Optional.absent();
    }
    FSMStateName next = handler.handle(state, es, is, event);
    return Optional.of(next);
  }
  
  public Optional<FSMStateName> handleNegative(FSMEvent event) throws FSMException {
    FSMBasicEventHandler handler = negativeHandlers.get(event.getClass());
    if (handler == null) {
      return Optional.absent();
    }
    FSMStateName next = handler.handle(state, es, is, event);
    return Optional.of(next);
  }
  
  public Optional<FSMStateName> handlePositive(FSMEvent payload, PatternExtractor<Class, FSMEvent> container)
    throws FSMException {
    FSMPatternEventHandler handler = positiveMsgHandlers.get(payload.getClass());
    if (handler == null) {
      return Optional.absent();
    }
    FSMStateName next = handler.handle(state, es, is, payload, container);
    return Optional.of(next);
  }
  
  public Optional<FSMStateName> handleNegative(FSMEvent payload, PatternExtractor<Class, FSMEvent> container)
    throws FSMException {
    FSMPatternEventHandler handler = negativeMsgHandlers.get(payload.getClass());
    if (handler == null) {
      return Optional.absent();
    }
    FSMStateName next = handler.handle(state, es, is, payload, container);
    return Optional.of(next);
  }
  
  public FSMStateName fallback(FSMEvent event, FSMBasicEventHandler fallback) throws FSMException {
    return fallback.handle(state, es, is, event);
  }

  public FSMStateName fallback(FSMEvent payload, PatternExtractor<Class, FSMEvent> container, FSMPatternEventHandler fallback) throws FSMException {
    return fallback.handle(state, es, is, payload, container);
  }
}
