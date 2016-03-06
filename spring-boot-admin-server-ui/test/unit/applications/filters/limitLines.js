'use strict';

var chai = require('chai')
  , expect = chai.expect;

var limitLines = require('../../../../modules/applications/filters/limitLines')();

describe('Filter: limitLines', function() {
    it('test limit 1', function() {
        expect(limitLines('1\n2\n3', 1)).to.equal('1');
        expect(limitLines('1', 1)).to.equal('1');
    });
    
    it('test limit 99', function() {
        expect(limitLines('1\n2\n3', 99)).to.equal('1\n2\n3');
    });
    
    it('test limit 1 begin 1', function() {
        expect(limitLines('1\n2\n3', 1, 1)).to.equal('2');
    });
});
