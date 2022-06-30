const express = require("express");
const {reset} = require("./../../../app/services/globals");
const request = require("supertest");
const config = require("../../../app/config")
const fs = require('fs')
const path = require("path");

describe('application', () => {
    let app;

    beforeEach(() => {
        app = express();
        app.use(express.json({limit: '500mb',}));
        app.use(require("./../../../app/router"))
        reset()
    });

    afterEach(() => {
        reset()
    });

    it("should accept global-filters configuration as a file", async () => {
        const res = await request(app)
            .post('/api/app/global-filters')
            .attach('filter', path.join(config.testResourcesDir, 'test-global-filters.conf'))
        expect(res.statusCode).toEqual(201)
        expect(fs.existsSync(config.globalFilterLocation)).toBe(true);
    });

    it("should error when configuration file is not attached", async () => {
        const res = await request(app)
            .post('/api/app/global-filters')
        expect(res.statusCode).toEqual(500)
        expect(res.text).toContain("Cannot read property")
    });

    it("should error when attachment 'key' is not named 'filter'", async () => {
        const res = await request(app)
            .post('/api/app/global-filters')
            .attach('global-filter', path.join(config.testResourcesDir, 'test-global-filters.conf'))
        expect(res.statusCode).toEqual(400)
        expect(res.text).toContain("No filter file present in request.")
    });

    it("should error when initialised without testSuite ", async () => {
        const res = await request(app)
            .post("/api/app/initialise")
            .set('Content-Type', 'application/json')
            .send({buildUrl: "https://dd.t.uk/"});
        expect(res.statusCode).toEqual(400)
    });

    it("should error when initialised without buildUrl ", async () => {
        const res = await request(app)
            .post("/api/app/initialise")
            .set('Content-Type', 'application/json')
            .send({testSuite: "awesome-tests-a11y-tests"});
        expect(res.statusCode).toEqual(400)
    });

    it("should accept page artifacts as a zip file", async () => {
        const res = await request(app)
            .post('/api/app/upload-page')
            .attach('zip', path.join(config.testResourcesDir, '1654074746541.zip'))
            expect(res.status).toEqual(201)
    });

    it("should error when page artifacts zip file attachment 'key' is not named 'zip'", async () => {
        const res = await request(app)
            .post('/api/app/upload-page')
            .attach('zip-test', path.join(config.testResourcesDir, '1654074746541.zip'))
            expect(res.statusCode).toEqual(400)
            expect(res.text).toContain("No pages zip file present in request.")
    });

});