angular.module("common", []);

angular.module("main", ["common"]).config(function ($parseProvider) {
	$parseProvider.unwrapPromises(true);
	$parseProvider.logPromiseWarnings(false);
});
