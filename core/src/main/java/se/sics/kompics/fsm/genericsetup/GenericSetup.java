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
package se.sics.kompics.fsm.genericsetup;

import java.util.List;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentProxy;
import se.sics.kompics.Handler;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.Negative;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.Port;
import se.sics.kompics.Positive;
import se.sics.kompics.fsm.FSMEvent;
import se.sics.kompics.network.Network;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class GenericSetup {

  private static final Logger LOG = LoggerFactory.getLogger(GenericSetup.class);

  public static void portsAndHandledEvents(ComponentProxy proxy,
    List<Pair<Class, List<Pair<Handler, Class>>>> positiveBasicEvents,
    List<Pair<Class, List<Pair<Handler, Class>>>> negativeBasicEvents,
    List<Pair<Class, List<Pair<ClassMatchedHandler, Class>>>> positivePatternEvents,
    List<Pair<Class, List<Pair<ClassMatchedHandler, Class>>>> negativePatternEvents) {

    for (Pair<Class, List<Pair<Handler, Class>>> e : positiveBasicEvents) {
      LOG.info("positive port:{}", e.getValue0());
      Positive port;
      try {
        port = proxy.getNegative(e.getValue0()).getPair();
      } catch (Exception ex) {
        port = proxy.requires(e.getValue0());
      }
      setupBasicEvent(proxy, port, e.getValue1());
    }
    for (Pair<Class, List<Pair<ClassMatchedHandler, Class>>> e : positivePatternEvents) {
      LOG.info("positive port:{}", e.getValue0());
      Positive port;
      try {
        port = proxy.getNegative(e.getValue0()).getPair();
      } catch (Exception ex) {
        port = proxy.requires(e.getValue0());
      }
      setupPatternEvent(proxy, port, e.getValue1());
    }
    for (Pair<Class, List<Pair<Handler, Class>>> e : negativeBasicEvents) {
      LOG.info("negative port:{}", e.getValue0());
      Negative port;
      try {
        port = proxy.getPositive(e.getValue0()).getPair();
      } catch(Exception ex) {
        port = proxy.provides(e.getValue0());
      }
      setupBasicEvent(proxy, port, e.getValue1());
    }
   for (Pair<Class, List<Pair<ClassMatchedHandler, Class>>> e : negativePatternEvents) {
       LOG.info("negative port:{}", e.getValue0());
      Negative port;
      try {
        port = proxy.getPositive(e.getValue0()).getPair();
      } catch(Exception ex) {
        port = proxy.provides(e.getValue0());
      }
      setupPatternEvent(proxy, port, e.getValue1());
    }
  }

  public static void handledEvents(ComponentProxy proxy,
    List<Pair<Class, List<Pair<Handler, Class>>>> positiveBasicEvents,
    List<Pair<Class, List<Pair<Handler, Class>>>> negativeBasicEvents,
    List<Pair<Class, List<Pair<ClassMatchedHandler, Class>>>> positivePatternEvents,
    List<Pair<Class, List<Pair<ClassMatchedHandler, Class>>>> negativePatternEvents) {

    for (Pair<Class, List<Pair<Handler, Class>>> e : positiveBasicEvents) {
      LOG.info("positive port:{}", e.getValue0());
      Positive port = proxy.getNegative(e.getValue0()).getPair();
      setupBasicEvent(proxy, port, e.getValue1());
    }
    for (Pair<Class, List<Pair<ClassMatchedHandler, Class>>> e : positivePatternEvents) {
      LOG.info("positive port:{}", e.getValue0());
      Positive port = proxy.getNegative(Network.class).getPair();
      setupPatternEvent(proxy, port, e.getValue1());
    }
    for (Pair<Class, List<Pair<Handler, Class>>> e : negativeBasicEvents) {
      LOG.info("negative port:{}", e.getValue0());
      Negative port = proxy.getPositive(e.getValue0()).getPair();
      setupBasicEvent(proxy, port, e.getValue1());
    }
    for (Pair<Class, List<Pair<ClassMatchedHandler, Class>>> e : negativePatternEvents) {
      LOG.info("negative port:{}", e.getValue0());
      Negative port = proxy.getPositive(e.getValue0()).getPair();
      setupPatternEvent(proxy, port, e.getValue1());
    }
  }

  private static void setupBasicEvent(ComponentProxy proxy, Port port, 
    List<Pair<Handler, Class>> handledBasicEvents) {
    for (final Pair<Handler, Class> e : handledBasicEvents) {
//      Handler handler = new Handler(e.getValue1()) {
//        @Override
//        public void handle(KompicsEvent event) {
//          e.getValue0().handle(event);
//        }
//      };
      proxy.subscribe(e.getValue0(), port);
    }
  }

  private static <C extends KompicsEvent & PatternExtractor<Class, FSMEvent>> void setupPatternEvent(ComponentProxy proxy, Port port,
    List<Pair<ClassMatchedHandler, Class>> handledPatternEvents) {
    for (final Pair<ClassMatchedHandler, Class> e : handledPatternEvents){
//      ClassMatchedHandler handler
//        = new ClassMatchedHandler<FSMEvent, C>(e.getValue1()) {
//          @Override
//          public void handle(FSMEvent payload, C container) {
//            e.getValue0().handle(payload, container);
//          }
//        };
      proxy.subscribe(e.getValue0(), port);
    }
  }
}
