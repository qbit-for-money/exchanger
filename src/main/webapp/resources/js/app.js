angular.module("common", ["ngResource"]);
angular.module('main-menu', ['ngRoute', 'angular-carousel']);

angular.module("main", ["common", 'main-menu', "ngRoute", "ui.bootstrap"]);
