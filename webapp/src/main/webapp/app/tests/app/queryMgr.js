var obj;
var qm = App.queryMgr;

App.cube = {
    name: '[Sales]'
};

function test_queryMgr(t) {

    t.plan(8);

    t.eq(qm.queries.length, 0, "Ok, queries empty");

    qm.createNewQuery();

    t.eq(qm.queries.length, 1, "OK, new query added to the queries");

    obj = {
        cube: {name: '[Sales]'},
        cols: [],
        rows: []
    };
    t.eq(qm.getQuery(), obj, "OK");

    qm.addColDimension('col1', 'level1');
    qm.addRowDimension('row1', 'level1');

    obj = {
        cube: {name: '[Sales]'},
        cols: [{
            dimension: 'col1',
            level: 'level1'
        }],
        rows: [{
            dimension: 'row1',
            level: 'level1'
        }]
    };
    t.eq(qm.getQuery(), obj, "OK");

    qm.addColDimension('col2', 'level1', 0);
    obj = {
        cube: {name: '[Sales]'},
        cols: [{
            dimension: 'col2',
            level: 'level1'
        }, {
            dimension: 'col1',
            level: 'level1'
        }],
        rows: [{
            dimension: 'row1',
            level: 'level1'
        }]
    };
    t.eq(qm.getQuery(), obj, "OK");

    qm.removeDimension('col2');
    obj = {
        cube: {name: '[Sales]'},
        cols: [{
            dimension: 'col1',
            level: 'level1'
        }],
        rows: [{
            dimension: 'row1',
            level: 'level1'
        }]
    };
    t.eq(qm.getQuery(), obj, "OK");

    qm.setMembers('row1', ['mb1', 'mb2']);
    obj = {
        cube: {name: '[Sales]'},
        cols: [{
            dimension: 'col1',
            level: 'level1'
        }],
        rows: [{
            dimension: 'row1',
            level: 'level1',
            members: ['mb1', 'mb2']
        }]
    };
    t.eq(qm.getQuery(), obj, "OK");

    qm.addMeasure('measure', 'measure1');
    obj = {
        cube: {name: '[Sales]'},
        cols: [{
            dimension: 'col1',
            level: 'level1'
        }],
        rows: [{
            dimension: 'row1',
            level: 'level1',
            members: ['mb1', 'mb2']
        }],
        measure: {
            dimension: 'measure',
            members: ['measure1']
        }
    };
    t.eq(qm.getQuery(), obj, "OK");
}
