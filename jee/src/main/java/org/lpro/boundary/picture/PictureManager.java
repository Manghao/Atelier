/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lpro.boundary.picture;

import java.util.UUID;
import org.lpro.entity.Picture;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author morgan
 */
public class PictureManager {
    
    @PersistenceContext
    EntityManager em;
    
    public Picture save(Picture p){
        p.setId(UUID.randomUUID().toString());
        return this.em.merge(p);
    }
}
