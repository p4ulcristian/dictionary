(ns dictionary.api
  "The request's language, and the page's addresses in every language: the
   server's side. The browser's is this namespace's .cljs half.

     (def settings (dictionary/check-settings {:languages         [{:code \"en\" :name \"English\"}   ; launched
                                                                {:code \"de\" :name \"Deutsch\"}]
                                               :default           \"en\"     ; its addresses carry no prefix
                                               :cookie-name       \"lang\"   ; or nil
                                               :preview-languages []       ; those that show here too: [{:code \"hu\" :name \"Magyar\"}] on a stage
                                               :fallback-language \"en\"     ; a message missing from a catalog
                                               :catalogs-path     \"i18n\"})) ; i18n/<language>.json, on the classpath

     (dictionary/find-language-of-request settings request)
     ;=> {:language \"de\" :path \"/orders\"}   ; for /de/orders, or /orders with a German browser
     (dictionary/make-alternate-links settings \"https://shop.example\" \"/orders\")
     ;=> [{:hreflang \"en\" :href \"https://shop.example/orders\"}
     ;    {:hreflang \"de\" :href \"https://shop.example/de/orders\"}
     ;    {:hreflang \"x-default\" :href \"https://shop.example/orders\"}]
     (dictionary/make-path-in-language settings \"/orders\" \"de\")   ;=> \"/de/orders\"
     (dictionary/list-languages-to-pick settings)
     ;=> [{:code \"en\" :name \"English\"} {:code \"de\" :name \"Deutsch\"}]   ; a language menu's

     ;; the texts: ICU MessageFormat messages, a flat JSON catalog per language
     (def texts (dictionary/load-texts settings))   ; once, at the start
     (dictionary/make-text texts \"de\" :orders.cart/add {:count 3})   ; the language is a value: an email's, a PDF's

   A request's language: the address's prefix (with more than one
   language), then the cookie the settings name, then Accept-Language, then
   the default. A message in a language: its catalog's, else the fallback
   language's, else the key's name, ugly on purpose. The settings are
   checked once, by check-settings, before the app starts; the functions
   trust them."
  (:require
   [dictionary.addresses :as addresses]
   [dictionary.catalogs :as catalogs]
   [dictionary.requests :as requests]
   [dictionary.settings :as settings]))

(def settings-shape
  "The settings, every key required: {:languages [{:code \"en\" :name
   \"English\"} {:code \"de\" :name \"Deutsch\"}] :default \"en\"
   :cookie-name \"lang\" …} (see dictionary.settings)."
  settings/settings-shape)

(defn check-settings
  "Returns the settings; throws {:type :dictionary/bad-settings :errors …}
   with every problem at once when they don't fit."
  [settings]
  (settings/check-settings settings))

(defn find-language-of-request
  "{:language … :path …}: the request's language, and its path (:uri)
   without the language's prefix, for routing."
  [settings request]
  (requests/find-language-of-request settings request))

(defn make-alternate-links
  "The page at path (no language in it) in every language, and x-default,
   the default's, each {:hreflang :href} with the origin in front; [] with
   one language."
  [settings origin path]
  (requests/make-alternate-links settings origin path))

(defn split-language-from-path
  "{:language \"de\" :path \"/orders\"} for /de/orders; {:language nil
   :path path} when the path starts with none (always, with one language)."
  [settings path]
  (addresses/split-language-from-path settings path))

(defn make-path-in-language
  "A path (no language in it) in a language: /de/orders; the default's
   carries none, /orders."
  [settings path language]
  (addresses/make-path-in-language settings path language))

(defn list-live-languages
  "The tags of the languages that show here: the launched :languages,
   then the :preview-languages this environment shows."
  [settings]
  (settings/list-live-languages settings))

(defn list-languages-to-pick
  "What a language menu lists: the languages that show here, in the
   settings' order, each its tag and its name in itself, [{:code \"en\"
   :name \"English\"} {:code \"de\" :name \"Deutsch\"} …]."
  [settings]
  (settings/list-languages-to-pick settings))

;; -----------------------------------------------------------------------------
;; Texts

(defn list-catalog-files
  "Every language that shows, its catalog's classpath resource and the
   address the browser fetches it at: [{:language \"de\" :resource
   \"i18n/de.json\" :address \"/i18n/de.json\"} …], for the app's server
   to serve."
  [settings]
  (catalogs/list-catalog-files settings))

(defn load-texts
  "Every language that shows, its catalog read from the classpath:
   {:fallback-language … :catalogs {language {key message}}}. A catalog
   that isn't there throws, :dictionary/missing-catalogs."
  [settings]
  (catalogs/load-texts settings))

(defn make-text
  "The key's ICU message in the language (a value: the request's, or the
   row's an email or a PDF is made for), args filled in ({} for none),
   by ICU4J; the fallback language's when the language's catalog lacks
   it; the key's name, \"orders.cart/add\", when neither has it."
  [texts language message-key args]
  (catalogs/make-text texts language message-key args))

(defn find-problems-in-catalogs
  "For an app's test: every key in every language, every message ICU can
   read. {} when so; else by language :missing-keys and :bad-messages,
   and :missing-catalogs.

     (is (= {} (dictionary/find-problems-in-catalogs settings)))"
  [settings]
  (catalogs/find-problems-in-catalogs settings))
