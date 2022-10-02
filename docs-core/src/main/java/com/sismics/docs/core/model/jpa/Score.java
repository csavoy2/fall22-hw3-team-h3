package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Score entity.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_SCORE")
public class Score implements Loggable {
    /**
     * Score ID.
     * NO COLUMN NAMES FIXED YET
     */
    @Id
    @Column(name = "SCO_ID_C", length = 36)
    private String id;
    
    /**
     * Document ID.
     */
    @Column(name = "SCO_IDDOC_C", length = 36, nullable = false)
    private String documentId;
    
    /**
     * User ID.
     */
    @Column(name = "SCO_IDUSER_C", length = 36, nullable = false)
    private String userId;
    
    /**
     * Content.
     */
    @Column(name = "SCO_CONTENT_C", nullable = false)
    private Integer content;
    
    /**
     * Creation date.
     */
    @Column(name = "SCO_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "SCO_DELETEDATE_D")
    private Date deleteDate;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }
    
    public Integer getContent() {
        return content;
    }

    public Score setContent(Integer content) {
        this.content = content;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("documentId", documentId)
                .add("userId", userId)
                .toString();
    }

    @Override
    public String toMessage() {
        return documentId;
    }
}
