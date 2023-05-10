#!/usr/bin/env node
const pug = require('pug')
const sass = require('node-sass')
const fs = require('fs')
const path = require('path')
const logger = require('../logger')
const config = require('../config')
const { applicationStatus } = require('./globals')

module.exports.generateHtmlReport = () => {
  return new Promise((res, rej) => {
      fs.readFile(path.join(config.outputDir, config.accessibilityAssessmentReportJson), 'utf8', (err, contents) => {
        if (err) {
          rej(err)
        } else {
          res(contents)
        }
      })
    }).then(JSON.parse)
    .then(prepareModel)
    .then(includeSass)
    .then(includeJs)
    .then(model => pug.renderFile(path.join(config.assetsDir, 'template.pug'), model))
    .then(writeReportToFileSystem)
    .then(applicationStatus("REPORT_READY"))
    .catch(e => {
      logger.log('ERROR', `Something went wrong when preparing report.  ${e.name} with message ${e.message}. \n${e.stack}`)
      applicationStatus("PAGE_ASSESSMENT_FAILED")
    })
}

const prepareModel = original => {
  let errorCountReducer = (acc, cur) => acc + cur.summary.errorCount;
  const tools = original.tools.map(x => ({
    total: x.summary.errorCount,
    techName: x.tool,
    version: x.version,
    name: x.name,
    paths: x.paths
  }))
  const totalErrorCount = original.tools.reduce(errorCountReducer, 0)

  return Object.assign({}, {
    original,
    totalErrorCount,
    tools
  })
}

const includeJs = model => (new Promise((res, rej) => {
  fs.readFile(path.join(`${config.assetsDir}`, 'report.js'), 'utf8', (err, contents) => {
    if (err) {
      rej(err)
    } else {
      const updatedModel = Object.assign({}, model);
      updatedModel.assets = Object.assign({}, model.assets, {
        javascript: [
	  contents,
	  fs.readFileSync(path.join(`${config.assetsDir}`, 'jquery.1.12.2.min.js'), 'utf8'),
	  fs.readFileSync(path.join(`${config.assetsDir}`, 'jquery.dataTables.1.13.4.min.js'), 'utf8'),
	  fs.readFileSync(path.join(`${config.assetsDir}`, 'table-filters.js'), 'utf8'),
	].join('\n\n')
      })
      res(updatedModel)
    }
  })
}))

const includeSass = model => (new Promise((res, rej) => {
    sass.render({
        file: `${config.assetsDir}/style.scss`
      },
      (err, result) => {
        if (err) {
          rej(err)
        } else {
          res(result.css.toString().split('\n').join('')
		  + '\n\n'
		  + fs.readFileSync(path.join(`${config.assetsDir}`, 'jquery.dataTables.1.13.4.min.css')))
        }
      })
  })
  .then(stylesheet => Object.assign({},
    model, {
      assets: Object.assign({},
        model.assets, {
          stylesheet
        }
      )
    })))

const writeReportToFileSystem = report => {
  fs.writeFile(path.join(config.outputDir, config.accessibilityAssessmentReportHtml), report, (err, data) => {
    if (err) {
      logger.log('ERROR', `An error occured when attempting to write the report to file system.`)
      throw err
    }
    logger.log('INFO', `HTML report ready.`)
  })
}
