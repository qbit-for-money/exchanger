var adminModule = angular.module("admin");

adminModule.factory("adminResource", function($resource) {
	return $resource(window.context + "webapi/admin", {}, {
		getBalance: {method: "GET", url: window.context + "webapi/admin/:currency/balance"},
		getWalletTransactions: {method: "GET", url: window.context + "webapi/admin/:currency/transactions"},
		sendMoney: {method: "POST"}
	});
});