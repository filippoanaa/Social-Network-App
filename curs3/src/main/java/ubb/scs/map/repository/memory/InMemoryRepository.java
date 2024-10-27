package ubb.scs.map.repository.memory;


import ubb.scs.map.domain.Entity;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.domain.exceptions.UserAlreadyExistsException;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.repository.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID,E> {

    protected Map<ID,E> entities;

    public InMemoryRepository() {
        entities=new HashMap<ID,E>();
    }

    @Override
    public Optional<E> findOne(ID id) {
        if(!entities.containsKey(id)){
            throw new EntityMissingException("Entity with ID: " + id + " does not exist");
        }
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }

    @Override
    public Optional<E> save(E entity) throws ValidationException {
        if(entity==null)
            throw new IllegalArgumentException("Entity cannot be null");
        if(entities.containsKey(entity.getId())){
            throw new UserAlreadyExistsException("User with username: " + entity.getId() + " already exists");
        }
        else{
            entities.put(entity.getId(),entity);
            return null;
        }


    }

    @Override
    public Optional<E> delete(ID id) {
        if(id==null){
            throw new IllegalArgumentException("Username cannot be null");
        }
        if(!entities.containsKey(id)){
            throw new UserMissingException("User with username: " + id + " does not exist");
        }
        return Optional.ofNullable(entities.remove(id));
    }


    @Override
    public Optional<E> update(E entity) {
        if(entity==null){
            throw new IllegalArgumentException("Entity cannnot be null");
        }
        if (!entities.containsKey(entity.getId()))
            throw new UserMissingException("User with username: " + entity.getId() + " does not exist");

        entities.put(entity.getId(),entity);

        return Optional.of(entity);
    }

    @Override
    public boolean exists(ID id) {
        return entities.containsKey(id);
    }
}
