
# page-capture-spike

This is a placeholder README.md for a new repository


## Server
### Start the server
Start the service from the `./server` directory with:
```bash
node server.js
```
The server will start on port 6001.

### Retrieve unique pages
Once a test run has been completed, and all of  the pages that you'd like to assess have been saved in the `./output` directory, execute the `identifyUniqueOutputFoldersByUrl.sh` script. Ensure that you change the `serverName` and `uniquePageDirectory` parameters.

You will also need to make sure that the output generated by the service is copied into the `service.log` file.  This output is required in order to sort and filter for unique URLs.


## Page Assessment
To assess all of the pages in a given folder (for example `./trusts-unique-pages`) execute `./assessAllPages.sh trusts-unique-pages` and all folders with names beginning with `1560` (timestamp) will have their `index.html` file assessed by axe, nu html validator and pa11y, and have reports generated in that same folder.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
