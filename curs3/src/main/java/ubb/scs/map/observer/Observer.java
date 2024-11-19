package ubb.scs.map.observer;

import ubb.scs.map.event.Event;

public interface Observer<E extends Event> {
    void update(E event);
}
