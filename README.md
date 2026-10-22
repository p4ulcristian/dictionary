# dictionary

Which language a request, a page and an address are in, for a Clojure app
on both sides. On the server it picks a request's language (the address's
prefix, `/de/orders`; a cookie; the browser's `Accept-Language`; the
default) and gives the page's alternate links; in the browser it reads the
language from an address and makes an address in a language. The texts of
a page in that language come later.

```clojure
io.github.p4ulcristian/dictionary {:git/tag "v0.1.0" :git/sha "3934a19"}
```

Needs only malli. One public namespace, `dictionary.api`: a `.clj` for the
server, a `.cljs` for the browser.

## The api

| side | name | does |
|---|---|---|
| both | `settings-shape` `check-settings` | the settings, every key required, checked once before use |
| server | `find-language-of-request` | `{:language :path}`: the request's language, and its path without the prefix |
| server | `make-alternate-links` | the page in every language, and `x-default`: `[{:hreflang :href} …]` |
| browser | `find-language-of-path` | the path's prefix's language, else the page's |
| both | `split-language-from-path` `make-path-in-language` | `/de/orders` ⇄ `{:language "de" :path "/orders"}` |

```clojure
(require '[dictionary.api :as dictionary])

(def settings (dictionary/check-settings {:languages   ["en" "de"]
                                          :default     "en"        ; its addresses carry no prefix
                                          :cookie-name "lang"}))   ; or nil: no cookie read

(dictionary/find-language-of-request settings {:uri "/de/orders" :headers {}})
;=> {:language "de" :path "/orders"}
(dictionary/find-language-of-request settings {:uri "/orders" :headers {"accept-language" "de-AT,de;q=0.9"}})
;=> {:language "de" :path "/orders"}
(dictionary/make-alternate-links settings "https://shop.example" "/orders")
;=> [{:hreflang "en" :href "https://shop.example/orders"}
;    {:hreflang "de" :href "https://shop.example/de/orders"}
;    {:hreflang "x-default" :href "https://shop.example/orders"}]

;; in the browser
(dictionary/find-language-of-path settings "/orders" "de")   ;=> "de", the page's
(dictionary/make-path-in-language settings "/orders" "de")   ;=> "/de/orders"
```

## Tests

```sh
clojure -X:test                                # the server's side
npm install && npx shadow-cljs compile test    # the browser's, on node
```

Why it is the way it is: [`decisions/`](decisions/README.md).

MIT license.
