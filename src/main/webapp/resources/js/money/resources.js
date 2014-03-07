var moneyModule = angular.module("money");

moneyModule.factory("walletsResource", function($resource) {
	return $resource(window.context + "webapi/wallets", {}, {
		generateAddress: {method: "GET", url: window.context + "wallets/:currency/generated-address"}
	});
});
