ace.define("ace/mode/java_worker", [], function(require, exports, module) {
    "use strict";
    var Mirror = require("ace/worker/mirror").Mirror;
    var JavaWorker = function(sender) {
        Mirror.call(this, sender);
        this.setTimeout(500);
    };
    JavaWorker.prototype = Object.create(Mirror.prototype);

    JavaWorker.prototype.onUpdate = function() {
        var value = this.doc.getValue();
        var errors = [];
        var lines = value.split("\n");
        lines.forEach(function(line, i) {
            if (line.includes("System.out.printl(")) {
                errors.push({
                    row: i,
                    column: line.indexOf("System.out.printl("),
                    text: "Did you mean println?",
                    type: "warning"
                });
            }
        });
        this.sender.emit("annotate", errors);
    };

    exports.JavaWorker = JavaWorker;
});
