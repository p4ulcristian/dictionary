# dictionary

Which language a request, a page and an address are in, for a Clojure app
on both sides. On the server it picks a request's language (the address's
prefix, `/de/orders`; a cookie; the browser's `Accept-Language`; the
default) and gives the page's alternate links; in the browser it reads the
language from an address and makes an address in a language. It lists
the languages a menu offers, each in its own name ("Deutsch"). And the
texts: ICU MessageFormat messages in a flat JSON catalog per language, the
same file read by the server (ICU4J: emails, PDFs) and fetched by the
browser (intl-messageformat, in the app's package.json); a key missing
from a language falls back to the fallback language, and missing there
shows as its name.

```clojure
io.github.p4ulcristian/dictionary {:git/tag "v0.3.0" :git/sha "96df8f3"}
```

Needs malli, jsonista and ICU4J; in the browser, intl-messageformat. One public namespace, `dictionary.api`: a `.clj` for the
server, a `.cljs` for the browser.

## The api

| side | name | does |
|---|---|---|
| both | `settings-shape` `check-settings` | the settings, every key required, checked once before use |
| server | `find-language-of-request` | `{:language :path}`: the request's language, and its path without the prefix |
| server | `make-alternate-links` | the page in every language, and `x-default`: `[{:hreflang :href} …]` |
| browser | `find-language-of-path` | the path's prefix's language, else the page's |
| both | `split-language-from-path` `make-path-in-language` | `/de/orders` ⇄ `{:language "de" :path "/orders"}` |
| both | `list-live-languages` | the tags of the launched languages, then of the preview ones that show here |
| both | `list-languages-to-pick` | the same languages, `{:code :name}` each, in the settings' order: what a language menu lists |
| server | `load-texts` `make-text` | every catalog that shows, read once; a message in a language, given as a value |
| server | `list-catalog-files` | each catalog's classpath resource and the address the browser fetches it at |
| server | `find-problems-in-catalogs` | for a test: every key in every language, every message ICU reads |
| browser | `fetch-catalog!` `make-text` | a language's catalog from the server; a message in a language |

```clojure
(require '[dictionary.api :as dictionary])

(def settings (dictionary/check-settings {:languages         [{:code "en" :name "English"}   ; launched, each in its own name
                                                              {:code "de" :name "Deutsch"}]
                                          :default           "en"     ; its addresses carry no prefix
                                          :cookie-name       "lang"   ; or nil: no cookie read (the app writes it)
                                          :preview-languages []       ; not launched, shown here: [{:code "hu" :name "Magyar"}] on a stage
                                          :fallback-language "en"     ; a message missing from a catalog
                                          :catalogs-path     "i18n"})) ; i18n/<language>.json

(dictionary/find-language-of-request settings {:uri "/de/orders" :headers {}})
;=> {:language "de" :path "/orders"}
(dictionary/find-language-of-request settings {:uri "/orders" :headers {"accept-language" "de-AT,de;q=0.9"}})
;=> {:language "de" :path "/orders"}
(dictionary/list-languages-to-pick settings)
;=> [{:code "en" :name "English"} {:code "de" :name "Deutsch"}]   ; a language menu's; flags are the app's
(dictionary/make-alternate-links settings "https://shop.example" "/orders")
;=> [{:hreflang "en" :href "https://shop.example/orders"}
;    {:hreflang "de" :href "https://shop.example/de/orders"}
;    {:hreflang "x-default" :href "https://shop.example/orders"}]

;; texts, on the server: the language is a value (the request's, an email's row's)
(def texts (dictionary/load-texts settings))
(dictionary/make-text texts "de" :orders.cart/add {:count 3})   ;=> "3 Artikel hinzufügen"
(is (= {} (dictionary/find-problems-in-catalogs settings)))      ; in the app's tests

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
