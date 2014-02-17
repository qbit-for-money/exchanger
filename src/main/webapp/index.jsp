<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html ng-app="main">
	<head>
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />
		
		<link rel="stylesheet" type="text/css" href="resources/lib/bootstrap/core/css/bootstrap.min.css">
		
		<link rel="stylesheet" type="text/css" href="resources/css/common.css">
		<link rel="stylesheet" type="text/css" href="resources/css/wizard.css">
		<link rel="stylesheet" type="text/css" href="resources/css/wizard-widget.css">
	</head>
	<body>
		<%@ include file="WEB-INF/jspf/nav.jspf" %>

		<div class="container">
			<div class="row">
				<div class="wizard" ng-controller="WizardController">
					<ul class="steps">
						<li ng-repeat="step in steps"
						    ng-class="{'active' : $index == currentStepIndex,
						    'complete' : $index < currentStepIndex}">
							<a href="" ng-click="setCurrentStep($index)" tabindex="-1">
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
		
		<script type="text/javascript" src="resources/lib/jquery/jquery-2.1.0.min.js"></script>
		<script type="text/javascript" src="resources/lib/angular/angular.min.js"></script>
		<script type="text/javascript" src="resources/lib/angular/angular-route.min.js"></script>
		<script type="text/javascript" src="resources/lib/angular/angular-resource.min.js"></script>
		<script type="text/javascript" src="resources/lib/angular/angular-touch.min.js"></script>
		<script type="text/javascript" src="resources/lib/bootstrap/core/js/ui-bootstrap-tpls-0.10.0.min.js"></script>

		<script type="text/javascript">
			// Global constants
	
			window.context = "${context}";
	
		</script>

		<script type="text/javascript" src="resources/js/app.js"></script>
		<script type="text/javascript" src="resources/js/common/common-service.js"></script>
		<script type="text/javascript" src="resources/js/common/common-controller.js"></script>
		<script type="text/javascript" src="resources/js/user/user-service.js"></script>
		<script type="text/javascript" src="resources/js/user/user-controller.js"></script>
		<script type="text/javascript" src="resources/js/order/order-service.js"></script>

		<script type="text/javascript" src="resources/js/wizard/wizard.js"></script>
		<script type="text/javascript" src="resources/js/wizard/currency.js"></script>
		<script type="text/javascript" src="resources/js/wizard/result.js"></script>
		<script type="text/javascript" src="resources/js/wizard/amount.js"></script>
	</body>
</html>
