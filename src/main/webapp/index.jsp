<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html ng-app="main">
	<head>
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />
		<%@ include file="WEB-INF/jspf/header.jspf" %>
	</head>
	<body>
		<div class="container" ng-controller="CommonController"  >
			<div class="row">
				<div class="col-md-12 ng-hide" ng-show="env.demoEnabled">
					<span class="label label-danger pull-right">demo</span>
				</div>
			</div>
			<%@ include file="WEB-INF/jspf/user.jspf" %>
			<div class="row">
				<div class="col-md-12">
					<p class="lead text-center">Use this document as a way to quickly start any new project.</p>
				</div>
			</div>
		</div>
	</body>
</html>
