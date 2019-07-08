package org.goobi.api.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.goobi.api.mail.StepConfiguration;
import org.goobi.api.mail.UserProjectConfiguration;
import org.goobi.beans.User;

import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.UserManager;

/**
 * This end point can be used to deactivate email notification for a single project, for all steps of a kind or at all.
 * 
 */
@Path("/mails/disable")
public class Emails {

    /**
     * 
     * @return
     */
    @Path("/all/{user}/{token}")
    @Produces(MediaType.APPLICATION_XHTML_XML)
    @GET // should be POST, but must be GET to have a clickable link within an email
    public Response deactivateEmailNotification(@PathParam("user") String userName, @PathParam("token") String token) {

        try {
            userName =  URLDecoder.decode(userName, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e2) {

        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("purpose", "disablemails");
        tokenMap.put("type", "all");
        tokenMap.put("user", userName);
        //        tokenMap.put("project", "");
        //        tokenMap.put("stepname", "");

        try {
            if (!JwtHelper.validateToken(token, tokenMap)) {
                return Response.status(404).build();
            }
        } catch (ConfigurationException e1) {
            return Response.status(500).build();
        }

        User user = UserManager.getUserByLogin(userName);
        user.lazyLoad();
        List<UserProjectConfiguration> configurationList = user.getEmailConfiguration();
        for (UserProjectConfiguration configuration : configurationList) {
            for (StepConfiguration step : configuration.getStepList()) {
                step.setActivated(false);
            }
        }
        try {
            UserManager.saveUser(user);
        } catch (DAOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Response.ok().build();
    }

    @Path("/step/{user}/{step}/{token}")
    @Produces(MediaType.APPLICATION_XHTML_XML)
    @GET // should be POST, but must be GET to have a clickable link within an email
    public Response deactivateEmailNotificationForStep(@PathParam("user") String userName, @PathParam("step") String step,
            @PathParam("token") String token) {

        try {
            userName =  URLDecoder.decode(userName, StandardCharsets.UTF_8.toString());
            step =  URLDecoder.decode(step, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e2) {

        }

        Map<String, String> deactivateStepMap = new HashMap<>();
        deactivateStepMap.put("purpose", "disablemails");
        deactivateStepMap.put("type", "step");
        deactivateStepMap.put("user", userName);
        deactivateStepMap.put("step", step);

        try {
            if (!JwtHelper.validateToken(token, deactivateStepMap)) {
                return Response.status(404).build();
            }
        } catch (ConfigurationException e1) {
            return Response.status(500).build();
        }

        User user = UserManager.getUserByLogin(userName);
        user.lazyLoad();
        List<UserProjectConfiguration> configurationList = user.getEmailConfiguration();
        for (UserProjectConfiguration configuration : configurationList) {
            for (StepConfiguration sc : configuration.getStepList()) {
                if (sc.getStepName().equals(step)) {
                    sc.setActivated(false);
                }
            }
        }
        try {
            UserManager.saveUser(user);
        } catch (DAOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Response.ok().build();
    }

    @Path("/project/{user}/{project}/{token}")
    @Produces(MediaType.APPLICATION_XHTML_XML)
    @GET // should be POST, but must be GET to have a clickable link within an email
    public Response deactivateEmailNotificationForProject(@PathParam("user") String userName, @PathParam("project") String project,
            @PathParam("token") String token) {

        try {
            userName =  URLDecoder.decode(userName, StandardCharsets.UTF_8.toString());
            project =  URLDecoder.decode(project, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e2) {

        }

        Map<String, String> deactivateProjectMap = new HashMap<>();
        deactivateProjectMap.put("purpose", "disablemails");
        deactivateProjectMap.put("type", "project");
        deactivateProjectMap.put("user", userName);
        deactivateProjectMap.put("project", project);

        try {
            if (!JwtHelper.validateToken(token, deactivateProjectMap)) {
                return Response.status(404).build();
            }
        } catch (ConfigurationException e1) {
            return Response.status(500).build();
        }

        User user = UserManager.getUserByLogin(userName);
        user.lazyLoad();
        List<UserProjectConfiguration> configurationList = user.getEmailConfiguration();
        for (UserProjectConfiguration configuration : configurationList) {
            if (configuration.getProjectName().equals(project)) {
                for (StepConfiguration step : configuration.getStepList()) {
                    step.setActivated(false);
                }
            }
        }
        try {
            UserManager.saveUser(user);
        } catch (DAOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Response.ok().build();
    }

}
