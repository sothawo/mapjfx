# branch notice

in this development branch I will try to implement a caching solution for the loaded map data, so that after having 
loaded data into the cache, offline usage will be possible. 

## first recherche results

it seems that WebView has no native cache support; one solution is to replace the URLStreamHandlerFactory and to 
implement a custom URLStreamHandler for http and https which will intercept the calls to the web and will doc the 
caching.
 
**Hint:** This means that **all** call to URL resources in the JVM are intercepted. 
