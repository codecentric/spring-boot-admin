'use strict';

var limitLines = require('../../../../modules/applications/filters/limitLines.js')();

describe('Filter: limitLines', function () {
    it('test limit 1', function () {
        expect(limitLines('1\n2\n3', 1)).toEqual('1');
        expect(limitLines('1', 1)).toEqual('1');
    });

    it('test limit 99', function () {
        expect(limitLines('1\n2\n3', 99)).toEqual('1\n2\n3');
    });

    it('test limit 1 begin 1', function () {
        expect(limitLines('1\n2\n3', 1, 1)).toEqual('2');
    });
});
