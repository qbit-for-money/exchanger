var moneyModule = angular.module("money");

moneyModule.factory("walletsResource", function($resource) {
	return $resource(window.context + "webapi/wallets", {}, {
		generateAddress: {method: "GET", url: window.context + "webapi/wallets/:currency/generated-address"},
		getBalance: {method: "GET", url: window.context + "webapi/wallets/:currency/balance"}
	});
});
