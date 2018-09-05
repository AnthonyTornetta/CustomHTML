<h1>Custom HTML</h1>
<h2>What is it?</h2>
<p>CustomHTML is a library that allows for custom HTML plugins to be added and compiled into normal HTML.</p>
<p>To use CustomHTML, simply download the CustomHTML jar file (found in the /latest directory) and any plugins you want to add to it.</p>
<p>The core plugin is found in the /plugins folder which contains custom tags developed by me.</p>
<p>To make your own tags, add the CustomHTML jar to your java build path, and add a plugin.yml file.</p>
<p>The plugin.yml should contain
<ul>
  <li>Main class location (e.g. com.domain.whatever.MyCoolTags)</li>
  <li>Author name</li>
  <li>Version</li>
  <li>Plugin's Name</li>
</ul>
</p>
<p>Take a look at the Core plugin to see how the plugin.yml is formatted</p>
<p>Your plugin's main class should extend the CustomHTMLPlugin class and override the enable method.</p>
<p>This method is called when your plugin is loaded and should add all the tags you want to the TagRegistry (using TagRegistry.register(new YourCustomTag(...)) or something similar)</p>
<p>All custom tags must implement ICustomTag.</p>
<p>Take a look at the Core plugin source to see how it works</p>

<h2>What Tags Are Present in the Core Plugin?</h2>

```html
<template src="/path/to/file.html" vars="varName='value'" /> <!-- Copies and pastes one file into this one -->
```

```html
<var name="varName" value="1234" /> - <!-- Stores the value of "1234" in a variable called "varName" -->
<var name="varName" /> - <!-- Compiles into 1234 -->
```

```html
<if x="$varName" equal y="1234">
  <h1>That var is 1234!</h1>
</if>
<if x="$varName" notEqual y="1234">
  <h1>That var isn't 1234!</h1>
</if>
```

^ Compiles into
```html
<h1>That var is 1234!</h1>
```
If the variable called "varName" is 1234, otherwise it'll spit out
```html
<h1>That var isn't 1234!</h1>
```
In an if block when referring to a variable as opposed to an actual String value, start the variable name with a $

If operators include: equal, notEqual, lessThan, greaterThan, greaterOrEqual, and lessOrEqual.
If a variable was never defined and used in a if tag, it will be the same as "undefined" in the if statement.
Thus to check if a variable is undefined, do
```html
<if x="$var" equals y="undefined">...</if>.
```
