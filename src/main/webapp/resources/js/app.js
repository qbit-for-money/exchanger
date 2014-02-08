angular.module("common", ["ngResource"]);
angular.module("main-menu", ["common", "ngRoute", "angular-carousel", "ngResource"]);

angular.module("main", ["common", 'main-menu', "ngRoute", "ui.bootstrap"]);
