<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first">
        <a href="${pageContext.request.contextPath}/admin">
            <spring:message code="admin.title.short" />
        </a>
    </li>

	<li <c:if test='<%= request.getRequestURI().contains("/manage") %>'>class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/muzimaconsultation/consultations.list#/">
            <spring:message code="muzimaconsultation.consultations" />
        </a>
	</li>
	
	<!-- Add further links here -->
</ul>
<h2>
	<spring:message code="muzimaconsultation.title" />
</h2>
