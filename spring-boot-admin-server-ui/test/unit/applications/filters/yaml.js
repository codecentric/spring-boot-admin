'use strict';

var chai = require('chai')
  , expect = chai.expect;

var yaml = require('../../../../modules/applications/filters/yaml')();

describe('Filter: yaml', function() {
    it('test yaml', function() {
        expect(yaml({ id: 1, obj : { name: 'foo' } })).to.equal('id: 1\nobj:\n  name: foo\n');
    });
});
