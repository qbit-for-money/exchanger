var wizardModule = angular.module("wizard");

wizardModule.controller("ModalDialogOpeningController", function($modal) {
	$modal.open({
		templateUrl: "resources/html/dialogs/auth-dialog.html",
		backdrop: "static",
		keyboard: false,
		backdropClick: false,
		dialogFade: false,
		placement: "bottom",
		controller: "ModalDialogController",
		windowClass: "child"
	});
});

wizardModule.controller("ModalDialogController", function($scope, modalResource) {
	var timestamp = new Date().getTime();
	$scope.auth = "True";
	$scope.pinInvalid = true;
	$scope.model = {};
	$scope.model.pin = "";
	$scope.model.publicKey = "";

	var destroyPinWatch = $scope.$watch('model.pin', function(newValue, oldValue) {
		if ((newValue.length === 4) && !isNaN(newValue)) {
			$scope.pinInvalid = false;
			var img = new Image();
			img.src = window.context + "webapi/captcha/image?pin=" + newValue + "&timestamp=" + timestamp;
			img.onload = function() {
				angular.element("#captcha").attr("src", img.src);
			};
		}
	}, true);

	$scope.change = function() {
		$scope.pinInvalid = true;
	};

	var destroyPubKeyWatch = $scope.$watch('model.publicKey', function(newValue, oldValue) {
		if (newValue !== "") {
			var authResponse = {publicKey: newValue, pin: $scope.model.pin, timestamp: timestamp};
			//var encrypted = modalResource.decrypt({publicKey: newValue, pin: $scope.model.pin, timestamp: timestamp});
			authResponse.$promise.then(function() {
				location.reload();
			});
		}
	}, true);

	$scope.$on("$destroy", function() {
		destroyPinWatch();
		destroyPubKeyWatch();
	});
});