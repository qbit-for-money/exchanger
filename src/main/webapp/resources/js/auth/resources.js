var wizardModule = angular.module("wizard");

wizardModule.factory("modalResource", function($resource) {
	return $resource(window.context + "webapi/captcha", {}, {
		auth: {method: "POST"}
	});
});
