var litecoinModule = angular.module("money.litecoin");

litecoinModule.factory("litecoinResource", function($resource) {
	return $resource(window.context + "webapi/litecoin/", {}, {
		getBalance: {method: "GET", url: window.context + "webapi/litecoin/balance"}
	});
});