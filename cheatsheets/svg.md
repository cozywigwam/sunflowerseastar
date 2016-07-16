---
layout: post
title: "SVG"
categories: cheatsheet
---

(W3C September 2010: SVG Primer)[https://www.w3.org/Graphics/SVG/IG/resources/svgprimer.html#SVG_in_HTML]
(W3C: Embedding SVG in HTML documents)[https://www.w3.org/Graphics/SVG/IG/resources/svgprimer.html#SVG_in_HTML]

```html
<!-- YES -->
<object type="image/svg+xml" data="image.svg">SVG is unsupported</object>

<!-- or -->
<object type="image/svg+xml" data="ovals.svg" width="320" height="240">
 <param name="src" value="ovals.svg">
</object>

<!-- or if you don't mind the verbosity -->
<svg width="300px" height="300px" xmlns="http://www.w3.org/2000/svg"> <text x="10" y="50" font-size="30">My SVG</text></svg>


<!-- MAYBE -->



<!-- NO -->

<!-- ha ha, iframe -->
<iframe src="image.svg">
  Your browser does not support iframes
</iframe>

<!-- not spec (W3C <embed>) -->
<embed type="image/svg+xml" src="image.svg" /> 

<!-- security; blocks links -->
<img src="image.svg" />
```

```css
// NO

// security; blocks links
#myelement {
  background-image: url(image.svg);
}
```


> 
> From (W3C: Embedded Content)[http://w3c.github.io/html/semantics-embedded-content.html#element]
> 
> Here’s a way to embed a resource that requires a proprietary plugin, like Flash:
> 
> ```
> <embed src="catgame.swf">
> > ```
> 
> If the user does not have the plugin (for example if the plugin vendor doesn’t support the user’s platform), then the user will be unable to use the resource.
> 
> To pass the plugin a parameter "quality" with the value "high", an attribute can be specified:
> 
> ```
> <embed src="catgame.swf" quality="high">
> ```
> 
> This would be equivalent to the following, when using an object element instead:
> 
> ```
> <object data="catgame.swf">
>   <param name="quality" value="high">
> </object>
> ```
> 
