var moneyModule = angular.module("amount");

moneyModule.factory("yandexResource", function($resource) {
	return $resource(window.context + "webapi/yandex/authorizeUrl", {}, {
		getAuthorizeUrl: {method: "GET"}
	});
});