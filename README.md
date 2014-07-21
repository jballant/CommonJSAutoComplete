CommonJSAutoComplete
====================

JavaScript plugin for Jetbrain's IDEs to provide AutoComplete support for CommonJS ```require``` statements. 

Setup:
------

For this plugin to work properly you will need to perform the following

1. Navigate to your IDE Preferences menu
2. Go to CommonJSAutoComplete under Project Settings
3. Set the relative path to your main JavaScript source code from the project root (leave blank if same as project root)
4. Set the relative path to your node_modules directory from the project root
5. List any modules that you would like to make all JavaScript files within the module available to this plugin
6. Check the "Use relative paths for files in Main JS Root Directory" checkbox if you need to make all require statements within the main javascript directory use relative paths
