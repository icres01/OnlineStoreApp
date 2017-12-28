// Define module dependencies.
var express = require('express');
var bodyParser = require('body-parser');
var cfenv = require("cfenv");
var path = require('path');
var cors = require('cors');

// Set-up Cloudant database service.
var appEnv = cfenv.getAppEnv();
cloudantService = appEnv.getService("CatalogDBService");
var items = require('./routes/items');

// Set-up middleware.
var app = express();
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Define location of index.html file (home page).
app.use(express.static(path.join(__dirname, 'public')));

// Define REST HTTP method handlers.
app.get('/db/:option', items.dbOptions);
app.get('/items', items.list);
app.get('/fib', items.fib);
app.get('/items/:id', items.find);
app.post('/items', items.create);
app.put('/items/:id', items.update);
app.delete('/items/:id', items.remove);

app.listen(appEnv.port, appEnv.bind);
console.log('App started on ' + appEnv.bind + ':' + appEnv.port);