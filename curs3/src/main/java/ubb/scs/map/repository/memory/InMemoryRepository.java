package ubb.scs.map.repository.memory;


import ubb.scs.map.domain.Entity;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.repository.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {

    protected Map<ID, E> entities;

    public InMemoryRepository() {
        entities = new HashMap<ID, E>();
    }

    @Override
    public Optional<E> findOne(ID id) {
        if(id == null)
            throw new IllegalArgumentException("Id cannot be null");
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }

    @Override
    public Optional<E> save(E entity) throws ValidationException {
        if (entity == null)
            throw new IllegalArgumentException("Entity cannot be null");
        if (entities.containsKey(entity.getId()))
            return Optional.of(entity);
        else {
            entities.put(entity.getId(), entity);
            return Optional.empty();
        }


    }

    @Override
    public Optional<E> delete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        E entity = entities.get(id);
        if(entity == null)
            return Optional.empty();
        return Optional.ofNullable(entities.remove(entity.getId()));

    }


    @Override
    public Optional<E> update(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        entities.put(entity.getId(), entity);
        if(entities.get(entity.getId()) != null) {
            entities.put(entity.getId(), entity);
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    @Override
    public boolean exists(ID id) {
        return entities.containsKey(id);
    }
}
