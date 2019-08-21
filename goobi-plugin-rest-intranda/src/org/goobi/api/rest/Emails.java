package org.goobi.api.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.goobi.api.mail.StepConfiguration;
import org.goobi.api.mail.UserProjectConfiguration;
import org.goobi.beans.User;

import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.UserManager;
import lombok.extern.log4j.Log4j;

/**
 * This end point can be used to deactivate email notification for a single project, for all steps of a kind or at all.
 * 
 */
@Path("/mails/disable")
@Log4j
public class Emails {

    /**
     * 
     * @return
     */
    @Path("/all/{user}/{token}")
    @Produces(MediaType.TEXT_HTML)
    @GET // should be POST, but must be GET to have a clickable link within an email
    public Response deactivateEmailNotification(@PathParam("user") String userName, @PathParam("token") String token,
            @Context HttpServletRequest request, @Context HttpServletResponse response) {

        try {
            userName = URLDecoder.decode(userName, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e2) {

        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("purpose", "disablemails");
        tokenMap.put("type", "all");
        tokenMap.put("user", userName);

        try {
            if (!JwtHelper.validateToken(token, tokenMap)) {
                //                return Response.status(404).build();
                return getErrorPage(Status.BAD_REQUEST, "invalid request, your url is worng or to old.");
            }
        } catch (ConfigurationException e1) {
            log.error(e1);
            //            return Response.status(500).build();
            return getErrorPage(Status.INTERNAL_SERVER_ERROR, "Configuration error, try again later.");
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
            log.error(e);
            return getErrorPage(Status.INTERNAL_SERVER_ERROR, "Save error, try again later.");
        }

        String myJsfPage = "/uii/mailNotificationDisabled.xhtml";
        String contextPath = request.getContextPath();
        try {
            response.sendRedirect(contextPath + myJsfPage);
        } catch (IOException e) {
            log.error(e);
        }
        return null;

    }

    @Path("/step/{user}/{step}/{token}")
    @Produces(MediaType.TEXT_HTML)
    @GET // should be POST, but must be GET to have a clickable link within an email
    public Response deactivateEmailNotificationForStep(@PathParam("user") String userName, @PathParam("step") String step,
            @PathParam("token") String token, @Context HttpServletRequest request, @Context HttpServletResponse response) {

        try {
            userName = URLDecoder.decode(userName, StandardCharsets.UTF_8.toString());
            step = URLDecoder.decode(step, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e2) {

        }

        Map<String, String> deactivateStepMap = new HashMap<>();
        deactivateStepMap.put("purpose", "disablemails");
        deactivateStepMap.put("type", "step");
        deactivateStepMap.put("user", userName);
        deactivateStepMap.put("step", step);

        try {
            if (!JwtHelper.validateToken(token, deactivateStepMap)) {
                return getErrorPage(Status.BAD_REQUEST, "invalid request, your url is worng or to old.");
            }
        } catch (ConfigurationException e1) {
            return getErrorPage(Status.INTERNAL_SERVER_ERROR, "Configuration error, try again later.");
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
            log.error(e);
            return getErrorPage(Status.INTERNAL_SERVER_ERROR, "Save error, try again later.");
        }

        String myJsfPage = "/uii/mailNotificationDisabled.xhtml";
        String contextPath = request.getContextPath();
        try {
            response.sendRedirect(contextPath + myJsfPage);
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    @Path("/project/{user}/{project}/{token}")
    @Produces(MediaType.TEXT_HTML)
    @GET // should be POST, but must be GET to have a clickable link within an email
    public Response deactivateEmailNotificationForProject(@PathParam("user") String userName, @PathParam("project") String project,
            @PathParam("token") String token, @Context HttpServletRequest request, @Context HttpServletResponse response) {

        try {
            userName = URLDecoder.decode(userName, StandardCharsets.UTF_8.toString());
            project = URLDecoder.decode(project, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e2) {

        }

        Map<String, String> deactivateProjectMap = new HashMap<>();
        deactivateProjectMap.put("purpose", "disablemails");
        deactivateProjectMap.put("type", "project");
        deactivateProjectMap.put("user", userName);
        deactivateProjectMap.put("project", project);

        try {
            if (!JwtHelper.validateToken(token, deactivateProjectMap)) {
                return getErrorPage(Status.BAD_REQUEST, "invalid request, your url is worng or to old.");
            }
        } catch (ConfigurationException e1) {
            return getErrorPage(Status.INTERNAL_SERVER_ERROR, "Configuration error, try again later.");
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
            log.error(e);
            return getErrorPage(Status.INTERNAL_SERVER_ERROR, "Save error, try again later.");
        }

        String myJsfPage = "/uii/mailNotificationDisabled.xhtml";
        String contextPath = request.getContextPath();
        try {
            response.sendRedirect(contextPath + myJsfPage);
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    private Response getErrorPage(Status status, String errorText) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<body>");
        sb.append("<p>");
        sb.append(errorText);
        sb.append("</p>");
        sb.append("</body>");
        sb.append("</html>");
        return Response.status(status).entity(sb.toString()).build();
    }
}
