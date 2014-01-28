<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<meta name="viewport" content="width=device-width, initial-scale=1.0">

<link rel="stylesheet" type="text/css" href="<c:url value="/resources/bootstrap/css/bootstrap.min.css" />">
<script type="text/javascript" src="<c:url value="/resources/jquery/jquery-2.1.0.min.js" />"></script>

<script type="text/javascript" src="<c:url value="/resources/bootstrap/js/bootstrap.min.js" />"></script>

<script type="text/javascript" src="<c:url value="/resources/angular/angular.min.js" />"></script>

<script type="text/javascript">
	// Global constants
	
	window.context = "<c:url value="/" />";
	
	jQuery.ajax({
		url: window.context + "webapi/env", async: false, success: function(env) {
			window.env = env;
		}, error: function() {
			console.warn("Error loading Env...");
		}
	});
</script>

<script type="text/javascript">
	// Common controller
	
	function Common($scope) {
		$scope.context = context;
		$scope.env = env;
	}
</script>