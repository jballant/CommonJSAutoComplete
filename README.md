CommonJSAutoComplete
====================

JavaScript plugin for Jetbrain's IDEs to provide AutoComplete support for CommonJS ```require``` and some ES6 ```import``` statements. 

Setup:
------

For this plugin to work properly you may need to perform the following

1. Navigate to your IDE Preferences menu
2. Go to CommonJSAutoComplete under Project Settings.
3. Set the relative path to your main JavaScript source code from the project root (leave blank if same as project root). For example, if you keep all of your javascript source files in "<project_root>/src/javascript", your completions will finish the require statement.
4. Set the relative path to your node_modules directory from the project root. Leave blank if your node modules directory is directly under the project root.
5. List any modules that you would like to make all JavaScript files within the module available to this plugin.
6. Check the "Use relative paths for files in Main JS Root Directory" checkbox if you need to make all require statements within the main javascript directory use relative paths. This will be checked by default, but if you use absolute paths relative from a project root in you require statements then you will want to uncheck this.

How to use it:
--------------

When this plugin is installed and configured, when you type a ```require``` var statement to load a dependency through a CommonJS file, the variable name is used to autocomplete the rest of the ```require``` statement:

```javascript

// In '<project_root>/src/javascript/views/MyView.js'

// MyModel.js is in <project_root>/src/javascript/models/MyModel
// and './src/js' is set as our main JS root directory. Then,
// when you type in "var MyModel = r" you should see the rest of
// the following require statement in your autocomplete options:
var MyModel = require('../models/javascript/MyModel');

```

The plugin also supports ES6 imports when importing the default export or all exports of a file:

```javascript

// typing out an import until you get the
// variable name...
import * from foo

// ...will allow you to autocomplete the path
import * as foo from '../utils/foo';

// Same for default exports
import Bar

// completes to...
import Bar from '../Bar.js'

```
