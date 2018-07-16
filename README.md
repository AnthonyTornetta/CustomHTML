<h1>Custom HTML</h1>
<h2>What is it?</h2>
CustomHTML contains a few tags not present in normal HTML that would make my life tons easier.
Thus, I added those tags.
These tags will compile down into normal HTML by running this code.

What Tags Are Present?

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
</h1>
```

^ Spits out "That var is 1234!" if the variable called "varName" is 1234, otherwise it'l spit out "That var isn't 1234!"

If operators include: equal, notEqual, lessThan, greaterThan, greaterOrEqual, and lessOrEqual.
If a variable was never defined and used in a if tag, it will be the same as null in the if statement.
Thus to check if a variable is undefined, do
```html
<if x="$var" equals y="null">...</if>.
```

Note: When I say "spit out" I mean compiles into.
