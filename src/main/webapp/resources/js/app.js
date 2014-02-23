angular.module("common", ["ngResource"]);

angular.module("user", ["ngResource"]);

angular.module("order", ["ngResource"]);

angular.module("money", []);

angular.module("exchange", ["ngResource"]);

angular.module("wizard", ["ngRoute"]);
angular.module("wizard.currency", ["common", "wizard", "order"]);
angular.module("wizard.amount", ["common", "wizard", "order"]);
angular.module("wizard.result", ["common", "wizard", "order"]);

angular.module("main", ["ngRoute", "ui.bootstrap", "common", "user", "order",
	"money", "exchange", "wizard", "wizard.currency", "wizard.amount", "wizard.result"]);

angular.module("main").config(function($routeProvider, $locationProvider) {
	$routeProvider.when("/steps/currency", {
		templateUrl: "resources/html/wizard/currency.html",
		controller: "CurrencyController"
	}).when("/steps/amount", {
		templateUrl: "resources/html/wizard/amount.html",
		controller: "AmountController"
	}).when("/steps/result", {
		templateUrl: "resources/html/wizard/result.html",
		controller: "ResultController"
	}).otherwise({redirectTo: "/steps/currency"});
	//$locationProvider.html5Mode(true);
}).run(function($rootScope, $location) {
	$rootScope.location = $location;
});