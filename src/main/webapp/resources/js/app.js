angular.module("common", ["ngResource"]);

angular.module("order", ["ngResource"]);

angular.module("main-menu", ["ngRoute", "angular-carousel", "ngResource", "common", "order"]);
angular.module("result", ["ngRoute", "ngResource", "common", "order"]);

angular.module("main", ["ngRoute", "ui.bootstrap", "common", "order", "main-menu", "result"])

angular.module("main").config(function($routeProvider, $locationProvider) {
	$routeProvider.when("/steps/currency", {
		templateUrl: "resources/html/currency-picker.html",
		controller: "MainMenuController"
	}).when("/steps/result", {
		templateUrl: "resources/html/result.html",
		controller: "MainMenuController"
	}).otherwise({ redirectTo: "/steps/currency" });
	//$locationProvider.html5Mode(true);
});;
