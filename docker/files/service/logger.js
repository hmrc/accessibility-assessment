const fs = require('fs')
const path = require('path')
const config = require('./config')

module.exports.log = function log(level, message) {
  const formatted_message = `{"level": "${level}", "message": "${message}", "type": "accessibility_logs", "app": "accessibility-assessment-service", "testSuite": "NotSet"}\n`
  console.log(formatted_message)
  fs.appendFile(path.join(config.outputDir, "accessibility-assessment-service.log"), formatted_message, (err, data) => {
    if (err) {
      throw err
    }
  })
}
