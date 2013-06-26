<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<h2><spring:message code="Manage Consultation"/></h2>
<div class="bootstrap-scope" ng-app="muzimaconsultation">
    <div ng-view ></div>
</div>
<openmrs:htmlInclude file="/moduleResources/muzimaconsultation/styles/flatui/bootstrap/css/bootstrap.css"/>
<openmrs:htmlInclude file="/moduleResources/muzimaconsultation/styles/flatui/js/bootstrap.min.js"/>
<openmrs:htmlInclude file="/moduleResources/muzimaconsultation/styles/flatui/css/flat-ui.css"/>

<openmrs:htmlInclude file="/moduleResources/muzimaconsultation/js/jquery/jquery.js"/>

<openmrs:htmlInclude file="/moduleResources/muzimaconsultation/js/angular/angular.js"/>
<openmrs:htmlInclude file="/moduleResources/muzimaconsultation/js/angular/angular-resource.js"/>

<openmrs:htmlInclude file="/moduleResources/muzimaconsultation/js/custom/app.js"/>
<openmrs:htmlInclude file="/moduleResources/muzimaconsultation/js/custom/controller.js"/>

<%@ include file="/WEB-INF/template/footer.jsp" %>

