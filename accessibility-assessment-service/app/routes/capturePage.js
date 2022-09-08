const router = require('express').Router();
const fs = require('fs')
const path = require('path')
const logger = require('../logger')
const config = require('../config')
const {capture, exclude, error} = require('../services/urls')
const {applicationStatus} = require('../services/globals')

router.post('/', (req, res, next) => {
    const body = req.body;
    const logData = Object.assign({}, body)
    const pageDirectory = path.join(config.pagesDirectory, '' + body.timestamp)
    logData.pageHTML = logData.pageHTML.substr(0, 100) + '...'
    logData.files = Object.keys(logData.files)
    const ALLOWED_STATUS = ["READY", "PAGES_CAPTURED"]
    const allowedStatusNotIncluded = (status) => {
        return !ALLOWED_STATUS.includes(status)
    }

    if (allowedStatusNotIncluded(global.status)) {
        logger.log('WARN', `Cannot capture page when status is ${global.status}. URL:${body.pageURL} will not be captured.`)
        return res.status(400).send({error: `Cannot capture page when status is ${global.status}.`})
    }

    if (pageIsNotHTML()) {
        logger.log('WARN', `Cannot assess non-HTML page. URL:${body.pageURL} will not be captured.`)
        exclude(body.pageURL)
        return res.status(400).send({error: `Cannot capture non-HTML page`})
    }

    if (urlIsAStub() && urlIsNotInAllowList()) {
        logger.log('WARN', `URL:${body.pageURL} contains the text 'stub'. This page will not be captured.`)
        exclude(body.pageURL)
        return res.status(400).send({error: "URL contains the text 'stub'. This page will not be captured."})
    }

    if (urlIsTestOnly() && urlIsNotInAllowList()) {
        logger.log('WARN', `URL:${body.pageURL} contains the text 'test-only'. This page will not be captured.`)
        exclude(body.pageURL)
        return res.status(400).send({error: "URL contains the text 'test-only'. This page will not be captured."})
    }

    if (urlIsAlreadyCapturedAndDoNotCaptureAllPages()) {
        logger.log('WARN', `URL:${body.pageURL} already captured'`)
        return res.status(400).send({error: "URL already captured."})
    }

    if(urlIsNotLocalhost()) {
        logger.log('WARN', `URL:${body.pageURL} is not using localhost. This page will not be captured.`)
        exclude(body.pageURL)
        return res.status(400).send({error: "URL is not using localhost. This page will not be captured."})
    }

    for (var assetError in logData.errors) {
        error(logData.errors[assetError].failedUrl, body.pageURL)
    }

    capture(body.pageURL)
    const fileList = Object.assign({}, body.files, {'index.html': body.pageHTML}, {'data': body.pageURL})
    fs.mkdirSync(pageDirectory, {recursive: true})

    Object.keys(fileList).forEach(fileName => {
        let fileExtension = path.extname(fileName);
        if (fileExtension !== ".js") {
            fs.writeFile(path.join(pageDirectory, fileName), fileList[fileName], (err, data) => {
                if (err) {
                    throw err
                }
                logger.log('INFO', `Captured ${fileName} for ${body.pageURL}`)
            })
        }
    })
    applicationStatus('PAGES_CAPTURED')
    res.status('201').send()

    function pageIsNotHTML() {
        let htmlContentRegEx = RegExp('<\\s*html[^>]*>([\\s\\S]*?)<\\s*\/\\s*html>');
        return !htmlContentRegEx.test(body.pageHTML)
    }

    function urlIsAStub() {
        let stubRegEx = RegExp('http:\/\/localhost:[0-9]{4}\/([a-z/-]+\-stub)');
        return stubRegEx.test(body.pageURL)
    }

    function urlIsTestOnly() {
        let testOnlyRegEx = RegExp('test\-only');
        return testOnlyRegEx.test(body.pageURL)
    }

    function urlIsNotInAllowList() {
        let allowListRegex = RegExp('http:\/\/localhost:[0-9]{4}\/(secure-message-stub)');
        return !allowListRegex.test(body.pageURL)
    }

    function urlIsAlreadyCapturedAndDoNotCaptureAllPages() {
        return global.capturedUrls.includes(body.pageURL) && !config.captureAllPages
    }

    function urlIsNotLocalhost() {
        let localhostRegEx = RegExp('http:\\/\\/localhost:[0-9]{4}\\/.*');
        return !localhostRegEx.test(body.pageURL)
    }
})

module.exports = router;
