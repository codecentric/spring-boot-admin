'use strict';

var capitalize = require('../../../../modules/applications-metrics/filters/capitalize.js')();

describe('Filter: capitalize', function () {
    it('test capitalize', function () {
        expect(capitalize('foo')).toEqual('Foo');
        expect(capitalize('foO')).toEqual('FoO');
        expect(capitalize('')).toEqual('');
        expect(capitalize(null)).toEqual(null);
    });
});
