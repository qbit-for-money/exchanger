var litecoinModule = angular.module("money.litecoin");

litecoinModule.factory("litecoinResource", function($resource) {
	return $resource(window.context + "webapi/litecoin/", {}, {
		getNewAddress: {method: "GET", url: window.context + "webapi/litecoin/getNewAddress"}
	});
});