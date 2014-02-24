var bitcoinModule = angular.module("money.bitcoin");

bitcoinModule.factory("bitcoinResource", function($resource) {
	return $resource(window.context + "webapi/bitcoin/", {}, {
		getNewAddress: {method: "GET", url: window.context + "webapi/bitcoin/getNewAddress"}
	});
});