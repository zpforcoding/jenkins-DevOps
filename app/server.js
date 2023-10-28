const express = require('express')
const pino = require('pino')
const path = require("path")


const app = express();

const logger = pino({
    level: 'info',
    timestamp: () => `,"time":"${new Date().toISOString()}"`
});

app.use("/static", express.static(path.join(__dirname, 'static')))
app.set('views', path.join(__dirname, 'views'));
app.set("view engine", "ejs")



app.get("/test", function (req, res) {
    res.render("test", { "name": "张学友", age: 20 })
})


logger.info('hello elastic world');
logger.info('This is some great stuff');
logger.info('Some more entries for our logging');
logger.info('another line');
logger.info('This never stops');
logger.info('Logging logging all the way');
logger.info('I think this is enough');
logger.info('nope, one more!');

app.listen(3000, function () {
    logger.info("app listening on port 3000!");
});
