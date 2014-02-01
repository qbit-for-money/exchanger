<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:url value="/" var="context" />

<link rel="stylesheet" type="text/css" href="${context}resources/lib/bootstrap/core/css/bootstrap.min.css">

<script type="text/javascript" src="${context}resources/lib/jquery/jquery-2.1.0.min.js"></script>
<script type="text/javascript" src="${context}resources/lib/bootstrap/core/js/bootstrap.min.js"></script>
<script type="text/javascript" src="${context}resources/lib/angular/angular.min.js"></script>

<script type="text/javascript">
	// Global constants
	
	window.context = "${context}";
	
</script>

<script type="text/javascript" src="${context}resources/js/app.js"></script>
<script type="text/javascript" src="${context}resources/js/common/common-service.js"></script>
<script type="text/javascript" src="${context}resources/js/common/common-controller.js"></script>