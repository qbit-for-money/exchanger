var moneyModule = angular.module("money.yandex");

moneyModule.factory("yandexResource", function($resource) {
	return $resource(window.context + "webapi/yandex/authorizeUrl", {}, {
		getAuthorizeUrl: {method: "GET"}
	});
});