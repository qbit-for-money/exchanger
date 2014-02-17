angular.module("common", ["ngResource"]);

angular.module("user", ["ngResource"]);
angular.module("order", ["ngResource"]);

angular.module("wizard", ["ngRoute", "common"]);
angular.module("currency", ["ngRoute", "ngResource", "common", "order", "wizard"]);
angular.module("result", ["ngRoute", "ngResource", "common", "user", "order"]);
angular.module("amount", ["ngRoute", "ngResource", "common", "user", "order"]);

angular.module("main", ["ngRoute", "ui.bootstrap", "common", "user", "order",
	"wizard", "currency", "result", "amount"]);

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
});

angular.module("main").run(function($rootScope, $location) {
	$rootScope.location = $location;
	$rootScope.orderInfo = {
		inTransfer: {
			type: "IN"
		},
		outTransfer: {
			type: "OUT"
		}
	};
});
