angular.module("common", ["ngResource"]);

angular.module("user", ["ngResource"]);

angular.module("money", []);
angular.module("order", ["ngResource", "common", "money"]);

angular.module("money.yandex", ["common", "order", "money"]).run(function(moneyCustomModules) {
	moneyCustomModules.put("YANDEX_RUB", this);
});

angular.module("exchange", ["ngResource"]);

angular.module("wizard", ["ngRoute", "common", "order"]);
angular.module("wizard.currency", ["common", "wizard", "user", "money", "order"]);
angular.module("wizard.amount", ["common", "wizard", "user", "money", "order", "exchange"]);
angular.module("wizard.result", ["common", "wizard", "user", "money", "order"]);

angular.module("main", ["ngRoute", "ui.bootstrap", "chieffancypants.loadingBar",
	"common", "user", "order", "money", "money.yandex", "exchange",
	"wizard", "wizard.currency", "wizard.amount", "wizard.result"]);

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
//	$locationProvider.html5Mode(true);
}).run(function($rootScope, $location) {
	$rootScope.location = $location;
});
