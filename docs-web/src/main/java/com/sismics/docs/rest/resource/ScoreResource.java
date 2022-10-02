package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.AclDao;
import com.sismics.docs.core.dao.ScoreDao;
import com.sismics.docs.core.dao.dto.ScoreDto;
import com.sismics.docs.core.model.jpa.Score;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.ImageUtil;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Score REST resource.
 * 
 * @author bgamard
 */
@Path("/score")
// have to fix this still
public class ScoreResource extends BaseResource {
    /**
     * Add a score.
     *
     * @api {put} /score Add a score
     * @apiName PutScore
     * @apiGroup Score
     * @apiParam {String} id Document ID
     * @apiParam {Number} content Score content
     * @apiSuccess {String} id Score ID
     * @apiSuccess {String} contentStr Content //needs work still
     * @apiSuccess {String} creator Username
     * @apiSuccess {String} creator_gravatar Creator Gravatar hash
     * @apiSuccess {Number} create_date Create date (timestamp)
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) NotFound Document not found
     * @apiPermission user
     * @apiVersion 1.5.0
     * 
     * @param documentId Document ID
     * @param content Score content
     * @return Response
     */
    @PUT
    public Response add(@FormParam("id") String documentId,
            @FormParam("content") String contentStr) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        ValidationUtil.validateRequired(documentId, "id");
        //THIS NO LONGER APPLIES
        Integer content = ValidationUtil.validateInteger(contentStr, "content");
        
        // Read access on doc gives access to write scores 
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(documentId, PermType.READ, getTargetIdList(null))) {
            throw new NotFoundException();
        }
        
        // Create the score
        Score score = new Score();
        score.setDocumentId(documentId);
        score.setContent(content);
        score.setUserId(principal.getId());
        ScoreDao scoreDao = new ScoreDao();
        scoreDao.create(score, principal.getId());
        
        // Returns the score
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", score.getId())
                .add("content", score.getContent())
                .add("creator", principal.getName())
                .add("creator_gravatar", ImageUtil.computeGravatar(principal.getEmail()))
                .add("create_date", score.getCreateDate().getTime());
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Delete a score.
     *
     * @api {delete} /score/:id Delete a score
     * @apiName DeleteScore
     * @apiGroup Score
     * @apiParam {String} id Score ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Score or document not found
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param id Score ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response delete(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the score
        ScoreDao scoreDao = new ScoreDao();
        Score score = scoreDao.getActiveById(id);
        if (score == null) {
            throw new NotFoundException();
        }
        
        // If the current user owns the score, skip ACL check
        if (!score.getUserId().equals(principal.getId())) {
            // Get the associated document
            AclDao aclDao = new AclDao();
            if (!aclDao.checkPermission(score.getDocumentId(), PermType.WRITE, getTargetIdList(null))) {
                throw new NotFoundException();
            }
        }
        
        // Delete the score
        scoreDao.delete(id, principal.getId());
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Get all scores on a document.
     *
     * @api {get} /score/:id Get scores
     * @apiName GetScore
     * @apiGroup Score
     * @apiParam {String} id Document ID
     * @apiParam {String} share Share ID
     * @apiSuccess {Object[]} scores List of scores
     * @apiSuccess {String} scores.id Score ID
     * @apiSuccess {String} scores.content Content
     * @apiSuccess {String} scores.creator Username
     * @apiSuccess {String} scores.creator_gravatar Creator Gravatar hash
     * @apiSuccess {Number} scores.create_date Create date (timestamp)
     * @apiError (client) NotFound Document not found
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @param documentId DocumentID
     * @return Response
     */
    @GET
    @Path("{documentId: [a-z0-9\\-]+}")
    public Response get(@PathParam("documentId") String documentId,
            @QueryParam("share") String shareId) {
        authenticate();
        
        // Read access on doc gives access to read scores 
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(documentId, PermType.READ, getTargetIdList(shareId))) {
            throw new NotFoundException();
        }
        
        // Assemble results
        ScoreDao scoreDao = new ScoreDao();
        List<ScoreDto> scoreDtoList = scoreDao.getByDocumentId(documentId);
        JsonArrayBuilder scores = Json.createArrayBuilder();
        for (ScoreDto scoreDto : scoreDtoList) {
            scores.add(Json.createObjectBuilder()
                    .add("id", scoreDto.getId())
                    .add("content", scoreDto.getContent())
                    .add("creator", scoreDto.getCreatorName())
                    .add("creator_gravatar", ImageUtil.computeGravatar(scoreDto.getCreatorEmail()))
                    .add("create_date", scoreDto.getCreateTimestamp()));
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("scores", scores);
        return Response.ok().entity(response.build()).build();
    }
}
