package com.sismics.docs.rest;

import java.util.Date;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Test;

import com.sismics.docs.core.model.jpa.Score;
import com.sismics.util.filter.TokenBasedSecurityFilter;

/**
 * Exhaustive test of the score resource.
 * 
 * @author bgamard
 */
public class TestScoreResource extends BaseJerseyTest {
    /**
     * Test the score resource.
     */
    @Test
    public void testScoreResource() {
        // Login score1
        clientUtil.createUser("score1");
        String score1Token = clientUtil.login("score1");
        
        // Login score2
        clientUtil.createUser("score2");
        String score2Token = clientUtil.login("score2");
        
        // Create a document with score1
        long create1Date = new Date().getTime();
        JsonObject json = target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score1Token)
                .put(Entity.form(new Form()
                        .param("title", "My super title document 1")
                        .param("description", "My super description for document 1")
                        .param("language", "eng")
                        .param("create_date", Long.toString(create1Date))), JsonObject.class);
        String document1Id = json.getString("id");
        Assert.assertNotNull(document1Id);
        
        // Create a score with score2 (fail, no read access)
        Response response = target().path("/score").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score2Token)
                .put(Entity.form(new Form()
                        .param("id", document1Id)
                        .param("content", "5")));
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
        
        // Read scores with score2 (fail, no read access)
        response = target().path("/score/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score2Token)
                .get();
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
        
        // Read scores with score 1
        json = target().path("/score/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score1Token)
                .get(JsonObject.class);
        Assert.assertEquals(0, json.getJsonArray("scores").size());
        
        // Create a score with score1
        json = target().path("/score").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score1Token)
                .put(Entity.form(new Form()
                        .param("id", document1Id)
                        .param("content", "5")), JsonObject.class);
        String score1Id = json.getString("id");
        Assert.assertNotNull(score1Id);
        Assert.assertEquals("5", json.getJsonNumber("content").toString());
        Assert.assertEquals("score1", json.getString("creator"));
        Assert.assertNotNull(json.getJsonNumber("create_date"));
        
        // Read scores with score1
        json = target().path("/score/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score1Token)
                .get(JsonObject.class);
        Assert.assertEquals(1, json.getJsonArray("scores").size());
        Assert.assertEquals(score1Id, json.getJsonArray("scores").getJsonObject(0).getString("id"));
        
        // Delete a score with score2 (fail, no write access)
        response = target().path("/score/" + score1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score2Token)
                .delete();
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
        
        // Delete a score with score1
        json = target().path("/score/" + score1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score1Token)
                .delete(JsonObject.class);
        
        // Read scores with score1
        json = target().path("/score/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score1Token)
                .get(JsonObject.class);
        Assert.assertEquals(0, json.getJsonArray("scores").size());
        
        // Add an ACL READ for score2 with score1
        json = target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "READ")
                        .param("target", "score2")
                        .param("type", "USER")), JsonObject.class);
        
        // Create a score with score2
        json = target().path("/score").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score2Token)
                .put(Entity.form(new Form()
                        .param("id", document1Id)
                        .param("content", "4")), JsonObject.class);
        String score2Id = json.getString("id");
        
        // Read scores with score2
        json = target().path("/score/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score2Token)
                .get(JsonObject.class);
        Assert.assertEquals(1, json.getJsonArray("scores").size());
        JsonObject score = json.getJsonArray("scores").getJsonObject(0);
        Assert.assertEquals(score2Id, score.getString("id"));
        Assert.assertEquals("4", score.getString("content"));
        Assert.assertEquals("score2", score.getString("creator"));
        Assert.assertNotNull(score.getJsonNumber("create_date"));
        
        // Delete a score with score2
        json = target().path("/score/" + score2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score2Token)
                .delete(JsonObject.class);
        
        // Read scores with score2
        json = target().path("/score/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, score2Token)
                .get(JsonObject.class);
        Assert.assertEquals(0, json.getJsonArray("scores").size());
    }
}