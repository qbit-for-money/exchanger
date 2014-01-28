<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html ng-app>
	<head>
		<jsp:include page="WEB-INF/jsp/header.jsp"/>
	</head>
	<body ng-controller="Common">
		<div class="container">
			<span class="label label-danger pull-right" ng-show="env.demoEnabled">demo</span>
			<p class="lead text-center">Use this document as a way to quickly start any new project.</p>
		</div>
	</body>
</html>
