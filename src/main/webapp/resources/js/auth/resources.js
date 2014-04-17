var wizardModule = angular.module("wizard");

wizardModule.factory("modalResource", function($resource) {
	return $resource(window.context + "webapi/captcha", {}, {
		encrypt: {method: "GET", url: window.context + "webapi/captcha/image"},
		//decrypt: {method: "GET", url: window.context + "webapi/captcha/decrypt"}
		auth: {method: "POST"}
	});
});
