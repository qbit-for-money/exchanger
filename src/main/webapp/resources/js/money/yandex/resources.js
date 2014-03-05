var yandexModule = angular.module("money.yandex");

yandexModule.factory("yandexResource", function($resource) {
	return $resource(window.context + "webapi/yandex/", {}, {
		getAuthorizeUrl: {method: "GET", url: window.context + "webapi/yandex/authorizeUrl"}
	});
});