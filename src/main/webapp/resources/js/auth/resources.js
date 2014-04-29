var captchaAuthModule = angular.module("captcha-auth");

captchaAuthModule.factory("captchaAuthResource", function($resource) {
	return $resource(window.context + "webapi/captcha-auth", {}, {
		getImage: {method: "GET", url: window.context + "webapi/captcha-auth/image"},
		auth: {method: "POST", url: window.context + "webapi/captcha-auth/auth"}
	});
});
