'use strict';

module.exports = function (Auth) {

    var interceptor = {
        request: function (request) {
            if (Auth.logged) {
                request.headers.Authorization = Auth.authorization();
            }
            return request;
        }
    };
    return interceptor;
};
        