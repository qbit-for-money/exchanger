<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html ng-app="main">
	<head>
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />

		<link rel="stylesheet" type="text/css" href="resources/lib/bootstrap/core/css/bootstrap.min.css">

		<link rel="stylesheet" type="text/css" href="resources/lib/angular/loading-bar.min.css">

		<link rel="stylesheet" type="text/css" href="resources/css/common.css">
		<link rel="stylesheet" type="text/css" href="resources/css/auth-dialog.css">
		<link rel="stylesheet" type="text/css" href="resources/css/wizard.css">
		<link rel="stylesheet" type="text/css" href="resources/css/wizard-widget.css">
	</head>
	<script>
		(function(i, s, o, g, r, a, m) {
			i['GoogleAnalyticsObject'] = r;
			i[r] = i[r] || function() {
				(i[r].q = i[r].q || []).push(arguments)
			}, i[r].l = 1 * new Date();
			a = s.createElement(o),
				m = s.getElementsByTagName(o)[0];
			a.async = 1;
			a.src = g;
			m.parentNode.insertBefore(a, m)
		})(window, document, 'script', '//www.google-analytics.com/analytics.js', 'ga');
		ga('create', 'UA-50199555-1', 'bitgates.com');
		ga('require', 'displayfeatures');
		ga('send', 'pageview');

	</script>
	<body>
		<div class="wrapper">
			<%@ include file="WEB-INF/jspf/nav.jspf" %>
			<%@ include file="WEB-INF/jspf/wizard.jspf" %>
			<div class="container" ng-if="user && orderInfo && (currentStepIndex >= 0)">
				<div ng-view></div>
			</div>
			<div class="push"></div>
		</div>
		<div class="footer navbar-default">&copy; Bitgates 2014</div>
		

		<script type="text/javascript" src="resources/lib/jquery/jquery-2.1.0.min.js"></script>
		<script type="text/javascript" src="resources/lib/jquery/jquery.mask.min.js"></script>
		<script type="text/javascript" src="resources/lib/angular/angular.min.js"></script>
		<script type="text/javascript" src="resources/lib/angular/angular-route.min.js"></script>
		<script type="text/javascript" src="resources/lib/angular/angular-resource.min.js"></script>
		<script type="text/javascript" src="resources/lib/angular/angular-touch.min.js"></script>
		<script type="text/javascript" src="resources/lib/bootstrap/core/js/ui-bootstrap-tpls-0.10.0.min.js"></script>
		<script type="text/javascript" src="resources/lib/angular/loading-bar.min.js"></script>
		<script type="text/javascript">
						// Global constants

						window.context = "${context}";

		</script>

		<script type="text/javascript" src="resources/js/app.js"></script>

		<script type="text/javascript" src="resources/js/common/resources.js"></script>
		<script type="text/javascript" src="resources/js/common/services.js"></script>
		<script type="text/javascript" src="resources/js/common/filters.js"></script>

		<script type="text/javascript" src="resources/js/user/resources.js"></script>
		<script type="text/javascript" src="resources/js/user/services.js"></script>
		<script type="text/javascript" src="resources/js/user/controllers.js"></script>

		<script type="text/javascript" src="resources/js/money/services.js"></script>
		<script type="text/javascript" src="resources/js/money/filters.js"></script>
		<script type="text/javascript" src="resources/js/money/directives.js"></script>
		<script type="text/javascript" src="resources/js/money/validation.js"></script>
		<script type="text/javascript" src="resources/js/money/resources.js"></script>

		<script type="text/javascript" src="resources/js/order/resources.js"></script>
		<script type="text/javascript" src="resources/js/order/services.js"></script>
		<script type="text/javascript" src="resources/js/order/validation.js"></script>

		<script type="text/javascript" src="resources/js/money/yandex/resources.js"></script>
		<script type="text/javascript" src="resources/js/money/yandex/controllers.js"></script>

		<script type="text/javascript" src="resources/js/exchange/resources.js"></script>

		<script type="text/javascript" src="resources/js/wizard/services.js"></script>
		<script type="text/javascript" src="resources/js/wizard/controllers.js"></script>

		<script type="text/javascript" src="resources/js/wizard/currency/controllers.js"></script>
		<script type="text/javascript" src="resources/js/wizard/currency/validation.js"></script>

		<script type="text/javascript" src="resources/js/wizard/amount/controllers.js"></script>
		<script type="text/javascript" src="resources/js/wizard/amount/validation.js"></script>

		<script type="text/javascript" src="resources/js/wizard/result/controllers.js"></script>
		<script type="text/javascript" src="resources/js/wizard/result/directives.js"></script>
		
		<script type="text/javascript" src="resources/js/auth/controllers.js"></script>
		<script type="text/javascript" src="resources/js/auth/resources.js"></script>
	</body>
</html>
