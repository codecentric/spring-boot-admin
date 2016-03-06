'use strict';

var chai = require('chai')
  , expect = chai.expect;

var joinArray = require('../../../../modules/applications-details/filters/joinArray')();

describe('Filter: joinArray', function() {
    it('test joinArray', function() {
        expect(joinArray([1,2], ', ')).to.equal('1, 2');
        expect(joinArray('Foo')).to.equal('Foo');
    });
});
