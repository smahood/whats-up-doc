## Notes

### Current Plan

1. Build everything assuming eager loading
-
-
1. Implement caching with localstorage
1. Look at what needs to change for lazy loading (if anything)





### Random thoughts

Really, I want to be able to pass in the markdown and parse it into hiccup (for a nice view) - seems like overkill

What does this all have to handle?
- Eager loading folders and documents
- Lazy loading documents
- Option for expandable links in ToC
- Option for links in ToC to change reading panel only


Experiment 1
- When clicking on ToC link, change reading panel
- When clicking on reading panel link, change ToC to match parent of clicked link and reading-panel to match clicked link

Experiment 2
- Click on link in ToC, expand and load data in ToC, load data to reading-panel and move browser to it

Experiment 3
- ToC for root doc on left, ToC for child on 3rd panel on right

Experiment 4
- Load one doc at a time based on how close to end (or clicking on link on Left)


Experiment 5
- Does an interceptor make sense to do the rendering after files have been fetched?


TODO - specify map of options that can be called from JS

Loading - Lazy or Eager

Root document - pass root doc in here, mandatory field

Option to display all docs as single stream or separate files

Would also be nice to have some graphical options - starting font size,
CSS classes, etc - or ways to override CSS classes so that instead of
using the default class you can provide your own classes or something

It would be very cool to be able to insert interceptors or middleware
that can be run when certain things are done (parsing or rendering the
TOC or markdown, etc.) - it would be interesting to think about whether
a plugin style architecture is at all reasonable, where all the rendering
or parsing is a specific plugin, or can plug in different fetching and
file parsing rules or something like that.

Debug or pre-deployment mode - check what kind of download sizes there
are, run against specs, etc. - maybe hook into frisk or similar

Setup nice error messages for malformed specs like figwheel has
for the config options

What kind of error messages are reasonable for end users to see?
"toc-navigation": "inline or ? (block? reading? add-to-toc, replace-reading, etc?)"

TODO -  Refer to Clojure Applied to figure out how I want to pass in and name the options arguments
