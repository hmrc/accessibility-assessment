const express = require('express')
const request = require('supertest')
const {reset, applicationStatus} = require('./../../../app/services/globals')
const fs = require('fs')
const path = require("path");
const config = require('./../../../app/config')
const {capture} = require("../../../app/services/urls");

describe('capturePage', () => {
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

    it.each([
        "READY",
        "PAGES_CAPTURED"
    ])('should capture a new HTML page page when the status is %s', async (status) => {
        applicationStatus(status);
        const res = await request(app)
            .post('/api/capture-page')
            .set('Content-Type', 'application/json')
            .send({
                pageURL: "http://localhost:1234/simple/page/capture",
                pageHTML: "<html><head><title>Some title</title></head><main>The contents of the page</main></html>",
                timestamp: "0000000002",
                files: {"file1": "some contents"}
            })
        expect(res.statusCode).toEqual(201)
        expect(global.status).toEqual('PAGES_CAPTURED')
        expect(global.capturedUrls).toEqual(["http://localhost:1234/simple/page/capture"])
    });

    it("should not capture a page with URL containing test-only", async () => {
        const res = await request(app)
            .post('/api/capture-page')
            .set('Content-Type', 'application/json')
            .send({
                pageURL: "http://localhost:1234/test-only/page",
                pageHTML: "<html><head><title>Some title</title></head><main>The contents of the page</main></html>",
                timestamp: "0000000002",
                files: {"file1": "some contents"}
            })
        expect(res.statusCode).toEqual(400)
        expect(global.status).toEqual('READY')
        expect(global.excludedUrls).toEqual(["http://localhost:1234/test-only/page"])
    });

    it("should not capture a page with URL containing -stub", async () => {
        const res = await request(app)
            .post('/api/capture-page')
            .set('Content-Type', 'application/json')
            .send({
                pageURL: "http://localhost:1234/my-stub/page",
                pageHTML: "<html><head><title>Some title</title></head><main>The contents of the page</main></html>",
                timestamp: "0000000002",
                files: {"file1": "some contents"}
            })
        expect(res.statusCode).toEqual(400)
        expect(global.status).toEqual('READY')
        expect(global.excludedUrls).toEqual(["http://localhost:1234/my-stub/page"])
    });

    it("should capture a page with URL matching allowListRegex", async () => {
        const res = await request(app)
            .post('/api/capture-page')
            .set('Content-Type', 'application/json')
            .send({
                pageURL: "http://localhost:1234/secure-message-stub/page",
                pageHTML: "<html><head><title>Some title</title></head><main>The contents of the page</main></html>",
                timestamp: "0000000002",
                files: {"file1": "some contents"}
            })
        expect(res.statusCode).toEqual(201)
        expect(global.status).toEqual('PAGES_CAPTURED')
        expect(global.capturedUrls).toEqual(["http://localhost:1234/secure-message-stub/page"])
    });

    it("should not capture a page which does not match htmlContentRegEx", async () => {
        const res = await request(app)
            .post('/api/capture-page')
            .set('Content-Type', 'application/json')
            .send({
                pageURL: "http://localhost:1234/simple/page/capture",
                pageHTML: '{"this page":"is not a HTML"}',
                timestamp: "0000000002",
                files: {"file1": "some contents"}
            })
        expect(res.statusCode).toEqual(400)
        expect(global.status).toEqual('READY')
        expect(global.excludedUrls).toEqual(["http://localhost:1234/simple/page/capture"])
    });

    it("should not capture a page with URL not localhost", async () => {
        const res = await request(app)
            .post('/api/capture-page')
            .set('Content-Type', 'application/json')
            .send({
                pageURL: "http://local:1234/my-page",
                pageHTML: "<html><head><title>Some title</title></head><main>The contents of the page</main></html>",
                timestamp: "0000000002",
                files: {"file1": "some contents"}
            })
        expect(res.statusCode).toEqual(400)
        expect(global.status).toEqual('READY')
        expect(global.excludedUrls).toEqual(["http://local:1234/my-page"])
    });

    it("should not capture js assets for a page", async () => {
        const res = await request(app)
            .post('/api/capture-page')
            .set('Content-Type', 'application/json')
            .send({
                pageURL: "http://localhost:1234/simple/page/capture",
                pageHTML: "<html><head><title>Some title</title></head><main>The contents of the page</main></html>",
                timestamp: "0000000002",
                files: {"file1.js": "some contents"}
            })
        expect(res.statusCode).toEqual(201)
        expect(global.status).toEqual('PAGES_CAPTURED')

        const pageDirectory = path.join(config.pagesDirectory, '' + "0000000002")
        expect(fs.existsSync(path.join(pageDirectory, "index.html"))).toBe(true);
        expect(fs.existsSync(path.join(pageDirectory, "file1.js"))).toBe(false);
    });

    it.each([
        "ASSESSING_PAGES",
        "REPORT_READY",
        "PAGE_ASSESSMENT_FAILED",
    ])('should NOT capture a page when the status is %s', async (status) => {
        applicationStatus(status);
        const res = await request(app)
            .post('/api/capture-page')
            .set('Content-Type', 'application/json')
            .send({
                pageURL: "http://localhost:1234/simple/page/capture",
                pageHTML: "<html><head><title>Some title</title></head><main>The contents of the page</main></html>",
                timestamp: "0000000002",
                files: {"file1": "some contents"}
            })
        expect(res.statusCode).toEqual(400)
        expect(global.status).toEqual(status)
        expect(global.capturedUrls).toEqual([])
    });

    it('should not capture a HTML page when it is already captured', async () => {
        capture("http://localhost:1234/simple/page/capture")
        applicationStatus('PAGES_CAPTURED');

        const res = await request(app)
            .post('/api/capture-page')
            .set('Content-Type', 'application/json')
            .send({
                pageURL: "http://localhost:1234/simple/page/capture",
                pageHTML: "<html><head><title>Some title</title></head><main>The contents of the page</main></html>",
                timestamp: "0000000002",
                files: {"file1": "some contents"}
            })
        expect(res.statusCode).toEqual(400)
        expect(global.status).toEqual('PAGES_CAPTURED')
        expect(global.capturedUrls).toEqual(["http://localhost:1234/simple/page/capture"])
    });

    it("should capture an already captured page when 'captureAllPages' is true", async () => {
        try {
            config.captureAllPages = true
            capture("http://localhost:1234/simple/page/capture")
            applicationStatus('PAGES_CAPTURED');

            const res = await request(app)
                .post('/api/capture-page')
                .set('Content-Type', 'application/json')
                .send({
                    pageURL: "http://localhost:1234/simple/page/capture",
                    pageHTML: "<html><head><title>Some title</title></head><main>The contents of the page</main></html>",
                    timestamp: "0000000002",
                    files: {"file1": "some contents"}
                })
            expect(res.statusCode).toEqual(201)
            expect(global.status).toEqual('PAGES_CAPTURED')
            expect(global.capturedUrls).toEqual(["http://localhost:1234/simple/page/capture", "http://localhost:1234/simple/page/capture"])
        } finally {
            config.captureAllPages = false
        }
    });
})