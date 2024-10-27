package ubb.scs.map.repository;


import ubb.scs.map.domain.Entity;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.EntityMissingException;

/**
 * CRUD operations repository interface
 * @param <ID> - type E must have an attribute of type ID
 * @param <E> -  type of entities saved in repository
 */

public interface Repository<ID, E extends Entity<ID>> {

    /**
     *
     * @param id -the id of the entity to be returned
     *           id must not be null
     * @return the entity with the specified id
     * @throws EntityMissingException if the entity with the given id does not exist
     */
    E findOne(ID id);

    /**
     *
     * @return all entities
     */
    Iterable<E> findAll();

    /**
     *
     * @param entity
     *         entity must be not null
     * @throws EntityAlreadyExistsException
     *             if the entity is already in repository
     * @throws IllegalArgumentException
     *             if the given entity is null.
     *
     */
    E save(E entity);


    /**
     *  removes the entity with the specified id
     * @param id
     *      id must be not null
     * @return the removed entity or null if there is no entity with the given id
     * @throws IllegalArgumentException
     *                   if the given id is null.
     */
    E delete(ID id);

    /**
     *
     * @param entity
     *          entity must not be null
     * @return null - if the entity is updated
     * @throws IllegalArgumentException
     *             if the given entity is null.
     * @throws EntityMissingException
     *                  if there is no user with the given id.
     */
    E update(E entity);

    boolean exists(ID id);

}

