package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.dto.ScoreDto;
import com.sismics.docs.core.model.jpa.Score;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Score DAO.
 * 
 * @author bgamard
 */
public class ScoreDao {
    /**
     * Creates a new score.
     * 
     * @param score Score
     * @param userId User ID
     * @return New ID
     */
    public String create(Score score, String userId) {
        // Create the UUID
        score.setId(UUID.randomUUID().toString());
        
        // Create the score
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        score.setCreateDate(new Date());
        em.persist(score);
        
        // Create audit log
        AuditLogUtil.create(score, AuditLogType.CREATE, userId);
        
        return score.getId();
    }
    
    /**
     * Deletes a score.
     * 
     * @param id Score ID
     * @param userId User ID
     */
    public void delete(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the score
        Query q = em.createQuery("select s from Score s where s.id = :id and c.deleteDate is null");
        q.setParameter("id", id);
        Score scoreDb = (Score) q.getSingleResult();
        
        // Delete the score
        Date dateNow = new Date();
        scoreDb.setDeleteDate(dateNow);

        // Create audit log
        AuditLogUtil.create(scoreDb, AuditLogType.DELETE, userId);
    }
    
    /**
     * Gets an active score by its ID.
     * 
     * @param id Score ID
     * @return Score
     */
    public Score getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select s from Score s where s.id = :id and s.deleteDate is null");
            q.setParameter("id", id);
            return (Score) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Get all scores on a document.
     * 
     * @param documentId Document ID
     * @return List of socres
     */
    public List<ScoreDto> getByDocumentId(String documentId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        //have to check database for this step 
        StringBuilder sb = new StringBuilder("select c.SCO_ID_C, c.SCO_CONTENT_C, c.SCO_CREATEDATE_D, u.USE_USERNAME_C, u.USE_EMAIL_C from T_SCORE c, T_USER u");
        sb.append(" where c.SCO_IDDOC_C = :documentId and c.SCO_IDUSER_C = u.USE_ID_C and c.SCO_DELETEDATE_D is null ");
        sb.append(" order by c.SCO_CREATEDATE_D asc ");
        
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("documentId", documentId);
        @SuppressWarnings("unchecked")
        List<Object[]> l = q.getResultList();
        
        List<ScoreDto> scoreDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            ScoreDto scoreDto = new ScoreDto();
            scoreDto.setId((String) o[i++]);
            scoreDto.setContent((String) o[i++]);
            scoreDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            scoreDto.setCreatorName((String) o[i++]);
            scoreDto.setCreatorEmail((String) o[i]);
            scoreDtoList.add(scoreDto);
        }
        return scoreDtoList;
    }
}
