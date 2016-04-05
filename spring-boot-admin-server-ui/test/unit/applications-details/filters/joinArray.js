'use strict';

var joinArray = require('../../../../modules/applications-details/filters/joinArray.js')();

describe('Filter: joinArray', function () {
    it('test joinArray', function () {
        expect(joinArray([1, 2], ', ')).toEqual('1, 2');
        expect(joinArray('Foo')).toEqual('Foo');
    });
});
