const router = require('express').Router();
var rimraf = require("rimraf");
const fs = require("fs");
const path = require('path')
const logger = require('../logger')
const { initialiseApp, reset, buildUrl, applicationStatus} = require('../services/globals')
const config = require('../config')
const fileupload = require('express-fileupload')
const util = require("util");
const exec = util.promisify(require('child_process').exec)

router.use(fileupload())

router.post('/initialise', async (req, res, next) => {
  if(!req.body.testSuite || !req.body.buildUrl) {
    let err = new Error(`Must supply a 'testSuite' and 'buildUrl' param in the request. Received testSuite:${req.body.testSuite} and buildUrl:${req.body.buildUrl}`)
    err.status = 400;
    return next(err);
  }
  await initialiseApp(req.body.testSuite, req.body.buildUrl);
  res.status(200).json({ applicationStatus: global.status, testSuite: global.testSuite, buildUrl: buildUrl()}).send();
})

router.post('/global-filters', function(req, res, next) {

  if (!req.files.filter) {
    let error = new Error("No filter file present in request.")
    error.status = 400
    return next(error)
  } else {
    fs.writeFile(config.globalFilterLocation, req.files.filter.data, 'utf8', (error, data) => {
      if (error) {
        return next(error);
      }
    })
    logger.log(`${config.globalFilterLocation} updated.`)
    res.status(201).send();
  }
})

router.post('/upload-page', async(req, res, next) => {

  if (!req.files.zip) {
    let error = new Error("No pages zip file present in request.")
    error.status = 400
    return next(error)
  } else {
    fs.mkdirSync(`${process.env.HOME}/pages/`, {recursive: true})
    fs.writeFile(`${process.env.HOME}/pages/file.zip` , req.files.zip.data, (error, data) => {
      if (error) {
        return next(error);
      }
    })
    await exec(`cd ${process.env.HOME}/pages/  &&  unzip -o file.zip && rm file.zip`, {maxBuffer: 1024 * 4096})
    applicationStatus('PAGES_CAPTURED')
    logger.log('INFO', `Extracted uploaded .zip file into ${config.pagesDirectory}`)
    res.status(201).send();
  }
})

router.post('/reset', async (req, res, next) => {
  reset();
  logger.log('INFO', 'Assessment image reset. All pages, configuration updates and reports have been deleted.')
  res.status(200).send();
})

module.exports = router;
