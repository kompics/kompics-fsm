/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics.fsm.core;

import java.util.Objects;
import se.sics.kompics.util.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class IntId implements Identifier {

  public final Integer id;

  public IntId(Integer id) {
    this.id = id;
  }

  @Override
  public int partition(int nrPartitions) {
    return id % nrPartitions;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + Objects.hashCode(this.id);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final IntId other = (IntId) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "" + id;
  }

  @Override
  public int compareTo(Identifier o) {
    IntId that = (IntId) o;
    return this.id.compareTo(that.id);
  }
}
