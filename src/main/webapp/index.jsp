<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html ng-app="main">
	<head>
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />
		<%@ include file="WEB-INF/jspf/head.jspf" %>
	</head>
	<body>
		<nav class="navbar navbar-default" role="navigation" ng-controller="EnvController">
			<div class="container-fluid">
				<!-- Brand and toggle get grouped for better mobile display -->
				<div class="navbar-header">
					<button type="button" class="navbar-toggle" ng-click="isNavbarCollapsed = !isNavbarCollapsed">
						<span class="sr-only">Toggle navigation</span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</button>
					<a class="navbar-brand" href="#">
						Bitgates <span class="label label-danger ng-hide" ng-show="env.demoEnabled">demo</span>
					</a>
				</div>
				<!-- Collect the nav links, forms, and other content for toggling -->
				<div class="collapse navbar-collapse" collapse="isNavbarCollapsed">
					<%@ include file="WEB-INF/jspf/user.jspf" %>
				</div><!-- /.navbar-collapse -->
			</div><!-- /.container-fluid -->
		</nav>
		<div class="container">
			<div class="row">
				<div class="wizard" ng-controller="WizardController">
					<ul class="steps">
						<li ng-repeat="step in steps"
						    ng-class="{'active' : step.path == location.path(),
						    'complete' : $index < getStepIndexByPath(location.path())}">
							<a ng-href="{{$index <= getStepIndexByPath(location.path()) ? ('#' + step.path) : ''}}">
								<span class="badge">{{$index + 1}}</span>
								{{step.title}}
								<span class="chevron"></span>
							</a>
						</li>
					</ul>
					<div class="actions">
						<button class="btn btn-lg btn-success" 
							ng-disabled="location.path() == steps[0].path"
							ng-click="goToPreviousStep()">Previous</button>
						<button class="btn btn-lg btn-success" 
							ng-disabled="location.path() == steps[steps.length - 1].path"
							ng-click="goToNextStep()">Next</button>
					</div>
				</div>
			</div>	
		</div>	
		<div class="container">
			<div ng-view></div>
		</div>
	</body>
</html>
