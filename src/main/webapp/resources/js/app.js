angular.module("common", ["ngResource"]);

angular.module("user", ["ngResource"]);
angular.module("order", ["ngResource"]);
angular.module("money", ["ngResource", "order", "common"], function($locationProvider) {
//      $locationProvider.html5Mode(true);
    });

angular.module("wizard", ["ngRoute", "common"]);
angular.module("currency", ["ngRoute", "ngResource", "common", "order"]);
angular.module("result", ["ngRoute", "ngResource", "common", "user", "order"]);

angular.module("main", ["ngRoute", "ui.bootstrap", "common", "user", "order",
	"wizard", "currency", "result", "money"]);

angular.module("main").config(function($routeProvider, $locationProvider) {
	$routeProvider.when("/steps/currency", {
		templateUrl: "resources/html/wizard/currency.html",
		controller: "CurrencyController"
	}).when("/steps/result", {
		templateUrl: "resources/html/wizard/result.html",
		controller: "ResultController"
	}).otherwise({ redirectTo: "/steps/currency" });
	//$locationProvider.html5Mode(true);
});
