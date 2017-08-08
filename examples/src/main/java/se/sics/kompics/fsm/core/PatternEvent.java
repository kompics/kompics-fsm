/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics.fsm.core;

import se.sics.kompics.PatternExtractor;
import se.sics.kompics.fsm.FSMEvent;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class PatternEvent<C extends FSMEvent> implements PatternExtractor<Class, C> {
  public final C content;
  
  public PatternEvent(C content) {
    this.content = content;
  }
  @Override
  public Class extractPattern() {
    return content.getClass();
  }

  @Override
  public C extractValue() {
    return content;
  }

  @Override
  public String toString() {
    return "PatternEvent<" + content.toString() + ">";
  }
}
